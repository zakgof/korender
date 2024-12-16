package com.zakgof.korender.impl.material

import com.zakgof.korender.KorenderException
import com.zakgof.korender.gl.GL.shaderEnv
import com.zakgof.korender.impl.engine.ShaderDeclaration
import com.zakgof.korender.impl.gpu.Gpu
import com.zakgof.korender.impl.resourceBytes

internal fun <T> MutableList<T>.peek(): T = this.last()
internal fun <T> MutableList<T>.pop(): T = this.removeAt(this.size - 1)

internal object Shaders {

    val imageQuadDeclaration: ShaderDeclaration =
        ShaderDeclaration("gui/image.vert", "gui/image.frag")

    fun create(declaration: ShaderDeclaration, gpu: Gpu) =
        ShaderBuilder(
            declaration.vertFile,
            declaration.fragFile,
            declaration.defs,
            declaration.plugins
        ).build(gpu)
}

private class ShaderBuilder(
    vertexShaderFile: String,
    fragmentShaderFile: String,
    defs: Set<String>,
    plugins: Map<String, String>
) {

    private val defs: Set<String> = defs + shaderEnv
    private val title: String = "$vertexShaderFile/$fragmentShaderFile"
    private val vertDebugInfo: ShaderDebugInfo = ShaderDebugInfo(vertexShaderFile)
    private val fragDebugInfo: ShaderDebugInfo = ShaderDebugInfo(fragmentShaderFile)

    private val vertCode: String =
        preprocessFile(vertexShaderFile, this.defs, vertDebugInfo, plugins)
    private val fragCode: String =
        preprocessFile(fragmentShaderFile, this.defs, fragDebugInfo, plugins)

    private fun preprocessFile(
        fname: String,
        defs: Set<String>,
        debugInfo: ShaderDebugInfo,
        plugins: Map<String, String>
    ): String {
        val content = resourceBytes("shader/$fname")
            .decodeToString()
        return preprocess(content, defs, fname, debugInfo, plugins)
    }

    fun preprocess(
        content: String,
        defs: Set<String>,
        fname: String,
        debugInfo: ShaderDebugInfo,
        plugins: Map<String, String>
    ): String {
        val outputLines = mutableListOf<String>()
        val ifdefs = mutableListOf<String>()
        val passes = mutableListOf<Boolean>()
        passes.add(true)
        debugInfo.start(fname)
        content.lines().forEach { it ->
            val line = it.trim()
            debugInfo.incSourceLine()
            preprocessLine(line, defs, ifdefs, passes, plugins, outputLines, debugInfo)
        }
        debugInfo.finish(fname)
        return outputLines.joinToString("\n") // TODO check on Linux
    }

    private fun preprocessLine(
        line: String,
        defs: Set<String>,
        ifdefs: MutableList<String>,
        passes: MutableList<Boolean>,
        plugins: Map<String, String>,
        outputLines: MutableList<String>,
        debugInfo: ShaderDebugInfo
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
            val includeContent = preprocessFile(includeFname, defs, debugInfo, plugins)
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

    fun build(gpu: Gpu) =
        gpu.createShader(title, vertCode, fragCode, vertDebugInfo, fragDebugInfo)

}
