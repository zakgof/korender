package com.zakgof.korender.impl.material

import com.zakgof.korender.KorenderException
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.impl.engine.ShaderDeclaration
import com.zakgof.korender.impl.gl.GL.shaderEnv
import com.zakgof.korender.impl.glgpu.GlGpuShader
import com.zakgof.korender.impl.resourceBytes

internal fun <T> MutableList<T>.peek(): T = this.last()
internal fun <T> MutableList<T>.pop(): T = this.removeAt(this.size - 1)

internal object Shaders {

    val imageQuadDeclaration: ShaderDeclaration =
        ShaderDeclaration("!shader/gui/image.vert", "!shader/gui/image.frag")

    suspend fun create(
        declaration: ShaderDeclaration,
        appResourceLoader: ResourceLoader
    ): GlGpuShader {
        val defs = declaration.defs + shaderEnv
        val title = "${declaration.vertFile}:${declaration.fragFile}"
        val vertDebugInfo = ShaderDebugInfo(declaration.vertFile)
        val fragDebugInfo = ShaderDebugInfo(declaration.fragFile)
        val vertCode =
            preprocessFile(declaration.vertFile, defs, vertDebugInfo, declaration.plugins, appResourceLoader)
        val fragCode =
            preprocessFile(declaration.fragFile, defs, fragDebugInfo, declaration.plugins, appResourceLoader)
        return GlGpuShader(title, vertCode, fragCode, vertDebugInfo, fragDebugInfo)
    }

    private suspend fun preprocessFile(
        fname: String,
        defs: Set<String>,
        debugInfo: ShaderDebugInfo,
        plugins: Map<String, String>,
        appResourceLoader: ResourceLoader
    ): String {
        val content = resourceBytes(appResourceLoader, fname).decodeToString()
        return preprocess(content, defs, fname, debugInfo, plugins, appResourceLoader)
    }

    private suspend fun preprocess(
        content: String,
        defs: Set<String>,
        fname: String,
        debugInfo: ShaderDebugInfo,
        plugins: Map<String, String>,
        appResourceLoader: ResourceLoader
    ): String {
        val outputLines = mutableListOf<String>()
        val ifdefs = mutableListOf<String>()
        val passes = mutableListOf<Boolean>()
        passes.add(true)
        debugInfo.start(fname)
        content.lines().forEach { it ->
            val line = it.trim()
            debugInfo.incSourceLine()
            preprocessLine(line, defs, ifdefs, passes, plugins, outputLines, debugInfo, appResourceLoader)
        }
        debugInfo.finish(fname)
        return outputLines.joinToString("\n") // TODO check on Linux
    }

    private suspend fun preprocessLine(
        line: String,
        defs: Set<String>,
        ifdefs: MutableList<String>,
        passes: MutableList<Boolean>,
        plugins: Map<String, String>,
        outputLines: MutableList<String>,
        debugInfo: ShaderDebugInfo,
        appResourceLoader: ResourceLoader
    ) {
        val ifdefMatcher = Regex("#ifdef (.+)").find(line)
        if (ifdefMatcher != null) {
            val defname = ifdefMatcher.groups[1]!!.value
            ifdefs.add(defname)
            passes.add(passes.peek() && defs.contains(defname))
            return
        }
        val ifndefMatcher = Regex("#ifndef (.+)").find(line)
        if (ifndefMatcher != null) {
            val defname = ifndefMatcher.groups[1]!!.value
            ifdefs.add("-$defname")
            passes.add(passes.peek() && !defs.contains(defname))
            return
        }
        if (line == "#else") {
            if (ifdefs.isEmpty())
                throw KorenderException("Shader preprocessor error: #else without #if")
            var inverting = ifdefs.pop()
            passes.pop()
            if (inverting.startsWith("!") || inverting.startsWith("+"))
                throw KorenderException("Shader preprocessor error: repeated #else for $inverting")
            if (inverting.startsWith("-")) {
                inverting = inverting.substring(1)
                ifdefs.add(inverting)
                passes.add(passes.peek() && defs.contains(inverting))
            } else {
                ifdefs.add("!$inverting")
                passes.add(passes.peek() && !defs.contains(inverting))
            }
            return
        }
        if (line == "#endif") {
            if (ifdefs.isEmpty())
                throw KorenderException("Shader preprocessor error: #endif without #if")
            ifdefs.pop()
            passes.pop()
            return
        }

        if (!passes.peek())
            return

        val includeMatcher = Regex("#import \"(.+)\"").find(line)
        if (includeMatcher != null) {
            val include = includeMatcher.groups[1]!!.value
            val includeFname = includeToFile(include, plugins)
            val includeContent = preprocessFile(
                includeFname,
                defs,
                debugInfo,
                plugins,
                appResourceLoader
            )
            outputLines.add(includeContent)
            return
        }
        outputLines.add(line)
        debugInfo.incDestLine(line)
    }

    private fun includeToFile(include: String, plugins: Map<String, String>): String {
        return if (include.startsWith("$")) {
            plugins[include.substring(1)]
                ?: throw KorenderException("Cannot find shader plugin $include")
        } else {
            include
        }
    }

}
