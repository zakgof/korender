package com.zakgof.korender.impl.material

import com.zakgof.korender.KorenderException
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.impl.engine.Loader
import com.zakgof.korender.impl.engine.ShaderDeclaration
import com.zakgof.korender.impl.gl.GL.shaderEnv
import com.zakgof.korender.impl.glgpu.GlGpuShader
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.resourceBytes
import impl.engine.ImmediatelyFreeRetentionPolicy

internal fun <T> MutableList<T>.peek(): T = this.last()
internal fun <T> MutableList<T>.pop(): T = this.removeAt(this.size - 1)

private class ShaderData(val title: String, val vertCode: String, val fragCode: String, val vertDebugInfo: ShaderDebugInfo, val fragDebugInfo: ShaderDebugInfo)

internal object Shaders {

    val imageQuadDeclaration: ShaderDeclaration =
        ShaderDeclaration("!shader/gui/image.vert", "!shader/gui/image.frag", retentionPolicy = ImmediatelyFreeRetentionPolicy)

    fun create(declaration: ShaderDeclaration, loader: Loader, zeroTex: GlGpuTexture): GlGpuShader? =
        loader.syncy(declaration) { load(declaration, it, zeroTex) } ?.let {
            GlGpuShader(it.title, it.vertCode, it.fragCode, it.vertDebugInfo, it.fragDebugInfo, zeroTex)
        }

    private suspend fun load(declaration: ShaderDeclaration, appResourceLoader: ResourceLoader, zeroTex: GlGpuTexture): ShaderData {
        val defs = declaration.defs + shaderEnv + declaration.plugins.keys.map {"PLUGIN_" + it.uppercase()}
        val title = "${declaration.vertFile}:${declaration.fragFile} $defs"
        val vertDebugInfo = ShaderDebugInfo(declaration.vertFile)
        val fragDebugInfo = ShaderDebugInfo(declaration.fragFile)
        val vertCode =
            preprocessFile(declaration.vertFile, defs, vertDebugInfo, declaration.plugins, appResourceLoader)
        val fragCode =
            preprocessFile(declaration.fragFile, defs, fragDebugInfo, declaration.plugins, appResourceLoader)
        return ShaderData(title, vertCode, fragCode, vertDebugInfo, fragDebugInfo)
    }

    private suspend fun preprocessFile(
        fname: String,
        defs: Set<String>,
        debugInfo: ShaderDebugInfo,
        plugins: Map<String, String>,
        appResourceLoader: ResourceLoader,
        includedFnames: MutableSet<String> = mutableSetOf()
    ): String {
        val content = resourceBytes(appResourceLoader, fname).decodeToString()
        return preprocess(content, defs, fname, debugInfo, plugins, appResourceLoader, includedFnames)
    }

    private suspend fun preprocess(
        content: String,
        defs: Set<String>,
        fname: String,
        debugInfo: ShaderDebugInfo,
        plugins: Map<String, String>,
        appResourceLoader: ResourceLoader,
        includedFnames: MutableSet<String>
    ): String {
        val outputLines = mutableListOf<String>()
        val ifdefs = mutableListOf<String>()
        val passes = mutableListOf<Boolean>()
        passes.add(true)
        debugInfo.start(fname)
        content.lines().forEach {
            val line = it.trim()
            debugInfo.incSourceLine()
            preprocessLine(line, defs, ifdefs, passes, plugins, outputLines, debugInfo, appResourceLoader, includedFnames)
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
        appResourceLoader: ResourceLoader,
        includedFnames: MutableSet<String>
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
            if (includedFnames.contains(includeFname))
                return
            includedFnames += includeFname;
            val includeContent = preprocessFile(
                includeFname,
                defs,
                debugInfo,
                plugins,
                appResourceLoader,
                includedFnames
            )
            outputLines.add(includeContent)
            return
        }
        outputLines.add(line)
        debugInfo.incDestLine(line)
    }

    private fun includeToFile(include: String, plugins: Map<String, String>): String {
        return if (include.startsWith("$")) {
            plugins[include.substring(1)] ?: throw KorenderException("Cannot find shader plugin $include")
        } else {
            include
        }
    }

}
