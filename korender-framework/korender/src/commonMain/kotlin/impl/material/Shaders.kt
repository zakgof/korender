package com.zakgof.korender.impl.material

import com.zakgof.korender.KorenderException
import com.zakgof.korender.Platform
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.impl.engine.Loader
import com.zakgof.korender.impl.engine.ShaderDeclaration
import com.zakgof.korender.impl.gl.GL.shaderEnv
import com.zakgof.korender.impl.glgpu.GlGpuShader
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.resourceBytes

internal fun <T> MutableList<T>.peek(): T = this.last()
internal fun <T> MutableList<T>.pop(): T = this.removeAt(this.size - 1)

private class ShaderData(val title: String, val vertCode: String, val fragCode: String, val vertDebugInfo: (String) -> String, val fragDebugInfo: (String) -> String)
private class Line(val text: String, val originFile: String, val originLine: Int)

internal object Shaders {

    fun create(declaration: ShaderDeclaration, loader: Loader, zeroTex: GlGpuTexture, zeroShadowTex: GlGpuTexture, uniformBufferHolder: UniformBufferHolder): GlGpuShader? =
        loader.syncy(declaration) { load(declaration, it) }?.let {
            GlGpuShader(it.title, it.vertCode, it.fragCode, it.vertDebugInfo, it.fragDebugInfo, zeroTex, zeroShadowTex, uniformBufferHolder)
        }

    private suspend fun load(declaration: ShaderDeclaration, appResourceLoader: ResourceLoader): ShaderData {
        val defs = declaration.defs + shaderEnv + declaration.plugins.keys.map { "PLUGIN_" + it.uppercase() }


        val shaderBaker = ShaderBaker(defs, declaration.plugins, appResourceLoader)
        return shaderBaker.load(declaration.vertFile, declaration.fragFile)
    }

    private class ShaderBaker(private val defs: Set<String>, private val plugins: Map<String, String>, private val appResourceLoader: ResourceLoader) {

        private val uniforms = mutableListOf<String>()

        suspend fun load(vertFile: String, fragFile: String): ShaderData {

            val vertLines = ShaderLoader().preprocessFile(vertFile)
            val fragLines = ShaderLoader().preprocessFile(fragFile)

            val uniformBlock = buildUniformBlock()
            injectUniforms(vertLines, uniformBlock)
            injectUniforms(fragLines, uniformBlock)

            val vertCode = vertLines.joinToString("\n") { it.text }
            val fragCode = fragLines.joinToString("\n") { it.text }

            val vertDebugInfo: (String) -> String = { debugInfo(it, vertLines) }
            val fragDebugInfo: (String) -> String = { debugInfo(it, fragLines) }

            return ShaderData("${vertFile}:${fragFile} $defs", vertCode, fragCode, vertDebugInfo, fragDebugInfo)
        }

        private fun buildUniformBlock(): List<String> {
            return if (uniforms.isEmpty())
                listOf()
            else
                mutableListOf("layout(std140) uniform Uniforms {") +
                        uniforms.distinct().map { "    $it" } +
                        "};"
        }

        private fun injectUniforms(shaderLines: MutableList<Line>, uniformBlock: List<String>) {
            val index = shaderLines.indexOfFirst { it.text.trim() == "#uniforms" }
            if (index >= 0) {
                shaderLines.removeAt(index)
                shaderLines.addAll(index, uniformBlock.map { Line(it, "", 0) })
            }
        }

        private fun debugInfo(log: String, lines: List<Line>): String {
            return log.lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .flatMap {
                    listOf(it, debugLineInfo(it, lines))
                }.joinToString("\n") { "       $it" }
        }

        private fun debugLineInfo(error: String, lines: List<Line>) =
            listOf(
                Regex("^(\\d+)\\((\\d+)\\).+$"),
                Regex("^.+: (\\d+):(\\d+):.+$"),
                Regex("(\\d+):(\\d+):.+$")
            ).map { it.find(error) }
                .firstOrNull { it != null }
                ?.let {
                    val row = it.groups[2]!!.value.toInt()
                    val offset = when (Platform.target) {
                        KorenderContext.TargetPlatform.Desktop -> 2
                        else -> 1
                    }
                    val entry = lines[row - offset]
                    val info = "[${entry.originFile}:${entry.originLine}]  ${entry.text}"
                    return info
                } ?: error

        private inner class ShaderLoader {

            private val includedFnames = mutableSetOf<String>()

            suspend fun preprocessFile(fname: String): MutableList<Line> {
                val content = resourceBytes(appResourceLoader, fname).decodeToString()
                return preprocess(content, fname)
            }

            private suspend fun preprocess(content: String, fname: String): MutableList<Line> {
                val outputLines = mutableListOf<Line>()
                val ifdefs = mutableListOf<String>()
                val passes = mutableListOf<Boolean>()
                passes.add(true)
                content.lines().forEachIndexed { originLine, lineText ->
                    preprocessLine(ifdefs, passes, outputLines, lineText.trim(), fname, originLine)
                }
                return outputLines
            }

            private suspend fun preprocessLine(
                ifdefs: MutableList<String>,
                passes: MutableList<Boolean>,
                outputLines: MutableList<Line>,
                lineText: String,
                originFile: String,
                originLine: Int
            ) {
                val ifdefMatcher = Regex("#ifdef (.+)").find(lineText)
                if (ifdefMatcher != null) {
                    val defname = ifdefMatcher.groups[1]!!.value
                    ifdefs.add(defname)
                    passes.add(passes.peek() && defs.contains(defname))
                    return
                }
                val ifndefMatcher = Regex("#ifndef (.+)").find(lineText)
                if (ifndefMatcher != null) {
                    val defname = ifndefMatcher.groups[1]!!.value
                    ifdefs.add("-$defname")
                    passes.add(passes.peek() && !defs.contains(defname))
                    return
                }
                if (lineText == "#else") {
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
                if (lineText == "#endif") {
                    if (ifdefs.isEmpty())
                        throw KorenderException("Shader preprocessor error: #endif without #if")
                    ifdefs.pop()
                    passes.pop()
                    return
                }

                if (!passes.peek())
                    return

                val includeMatcher = Regex("#import \"(.+)\"").find(lineText)
                if (includeMatcher != null) {
                    val include = includeMatcher.groups[1]!!.value
                    val includeFname = includeToFile(include, plugins)
                    if (includedFnames.contains(includeFname))
                        return
                    includedFnames += includeFname;
                    val includeContent = preprocessFile(includeFname)
                    outputLines.addAll(includeContent)
                    return
                }

                val uniformMatcher = Regex("#uniform (.+)").find(lineText)
                if (uniformMatcher != null) {
                    val uniform = uniformMatcher.groups[1]!!.value
                    uniforms += uniform
                    return
                }

                outputLines.add(Line(lineText, originFile, originLine))
            }

            private fun includeToFile(include: String, plugins: Map<String, String>): String {
                return if (include.startsWith("$")) {
                    plugins[include.substring(1)] ?: throw KorenderException("Cannot find shader plugin $include")
                } else {
                    include
                }
            }
        }
    }
}
