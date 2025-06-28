package com.zakgof.korender.impl.material

import com.zakgof.korender.KorenderException
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.impl.engine.Loader
import com.zakgof.korender.impl.engine.ShaderDeclaration
import com.zakgof.korender.impl.gl.GL.shaderEnv
import com.zakgof.korender.impl.glgpu.GlGpuShader
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.glgpu.GlGpuUniformBuffer
import com.zakgof.korender.impl.resourceBytes

internal fun <T> MutableList<T>.peek(): T = this.last()
internal fun <T> MutableList<T>.pop(): T = this.removeAt(this.size - 1)

private class ShaderData(val title: String, val vertCode: String, val fragCode: String, val vertDebugInfo: ShaderDebugInfo, val fragDebugInfo: ShaderDebugInfo)

internal object Shaders {

    fun create(declaration: ShaderDeclaration, loader: Loader, zeroTex: GlGpuTexture, zeroShadowTex: GlGpuTexture, frameUbo: GlGpuUniformBuffer): GlGpuShader? =
        loader.syncy(declaration) { load(declaration, it) }?.let {
            GlGpuShader(it.title, it.vertCode, it.fragCode, it.vertDebugInfo, it.fragDebugInfo, zeroTex, zeroShadowTex, frameUbo)
        }

    private suspend fun load(declaration: ShaderDeclaration, appResourceLoader: ResourceLoader): ShaderData {
        val defs = declaration.defs + shaderEnv + declaration.plugins.keys.map { "PLUGIN_" + it.uppercase() }


        val shaderBaker = ShaderBaker(defs, declaration.plugins, appResourceLoader)
        return shaderBaker.load(declaration.vertFile, declaration.fragFile)
    }

    fun frame(): GlGpuUniformBuffer = GlGpuUniformBuffer(
        4608, mapOf(
            "cameraPos" to 0,
            "cameraDir" to 16,
            "view" to 32,
            "projection" to 96,
            "screenWidth" to 160,
            "screenHeight" to 164,
            "time" to 168,
            "ambientColor" to 176,
            "numDirectionalLights" to 188,
            "directionalLightDir[0]" to 192,
            "directionalLightColor[0]" to 704,
            "directionalLightShadowTextureIndex[0]" to 1216,
            "directionalLightShadowTextureCount[0]" to 1728,
            "numPointLights" to 2240,
            "pointLightPos[0]" to 2256,
            "pointLightColor[0]" to 2768,
            "pointLightAttenuation[0]" to 3280,
            "numShadows" to 3792,
            "bsps[0]" to 3808,
            "cascade[0]" to 4128,
            "yMin[0]" to 4208,
            "yMax[0]" to 4288,
            "shadowMode[0]" to 4368,
            "f1[0]" to 4448,
            "i1[0]" to 4528
        )
    )

    private class ShaderBaker(private val defs: Set<String>, private val plugins: Map<String, String>, private val appResourceLoader: ResourceLoader) {

        private val uniforms = mutableListOf<String>()

        suspend fun load(vertFile: String, fragFile: String): ShaderData {
            val vertDebugInfo = ShaderDebugInfo()
            val fragDebugInfo = ShaderDebugInfo()
            val vertShaderLoader = ShaderLoader(vertDebugInfo)
            val fragShaderLoader = ShaderLoader(fragDebugInfo)
            val vertCode = vertShaderLoader.preprocessFile(vertFile)
            val fragCode = fragShaderLoader.preprocessFile(fragFile)

            val uniformBlock = buildUniformBlock()
            val postProcessedVertCode = vertCode.replace("#uniforms", uniformBlock)
            val postProcessedFragCode = fragCode.replace("#uniforms", uniformBlock)

            return ShaderData("${vertFile}:${fragFile} $defs", postProcessedVertCode, postProcessedFragCode, vertDebugInfo, fragDebugInfo)
        }

        private fun buildUniformBlock() = if (uniforms.isEmpty()) "" else buildString {
            append("layout(std140) uniform Uniforms {\n")
            uniforms.distinct().forEach { append("    ").append(it).append("\n") }
            append("};")
        }

        private inner class ShaderLoader(val debugInfo: ShaderDebugInfo) {

            private val includedFnames = mutableSetOf<String>()

            suspend fun preprocessFile(fname: String): String {
                val content = resourceBytes(appResourceLoader, fname).decodeToString()
                return preprocess(content, fname)
            }

            private suspend fun preprocess(content: String, fname: String): String {
                val outputLines = mutableListOf<String>()
                val ifdefs = mutableListOf<String>()
                val passes = mutableListOf<Boolean>()
                passes.add(true)
                debugInfo.start(fname)
                content.lines().forEach {
                    val line = it.trim()
                    debugInfo.incSourceLine()
                    preprocessLine(line, ifdefs, passes, outputLines)
                }
                debugInfo.finish(fname)
                return outputLines.joinToString("\n") // TODO check on Linux
            }

            private suspend fun preprocessLine(
                line: String,
                ifdefs: MutableList<String>,
                passes: MutableList<Boolean>,
                outputLines: MutableList<String>,
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
                    val includeContent = preprocessFile(includeFname)
                    outputLines.add(includeContent)
                    return
                }

                val uniformMatcher = Regex("#uniform (.+)").find(line)
                if (uniformMatcher != null) {
                    val uniform = uniformMatcher.groups[1]!!.value
                    uniforms += uniform
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
    }
}
