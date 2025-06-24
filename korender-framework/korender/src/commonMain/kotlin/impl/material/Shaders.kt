package com.zakgof.korender.impl.material

import com.zakgof.korender.KorenderException
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.impl.engine.Loader
import com.zakgof.korender.impl.engine.ShaderDeclaration
import com.zakgof.korender.impl.gl.GL.shaderEnv
import com.zakgof.korender.impl.glgpu.GlGpuShader
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.resourceBytes

internal fun <T> MutableList<T>.peek(): T = this.last()
internal fun <T> MutableList<T>.pop(): T = this.removeAt(this.size - 1)

private class ShaderData(val title: String, val vertCode: String, val fragCode: String, val vertDebugInfo: ShaderDebugInfo, val fragDebugInfo: ShaderDebugInfo)

internal object Shaders {

    fun create(declaration: ShaderDeclaration, loader: Loader, zeroTex: GlGpuTexture, zeroShadowTex: GlGpuTexture): GlGpuShader? =
        loader.syncy(declaration) { load(declaration, it) }?.let {
            GlGpuShader(it.title, it.vertCode, it.fragCode, it.vertDebugInfo, it.fragDebugInfo, zeroTex, zeroShadowTex)
        }

    private suspend fun load(declaration: ShaderDeclaration, appResourceLoader: ResourceLoader): ShaderData {
        val defs = declaration.defs + shaderEnv + declaration.plugins.keys.map { "PLUGIN_" + it.uppercase() }
        val title = "${declaration.vertFile}:${declaration.fragFile} $defs"
        val vertDebugInfo = ShaderDebugInfo(declaration.vertFile)
        val fragDebugInfo = ShaderDebugInfo(declaration.fragFile)
        val vertCode = ShaderLoader(defs, vertDebugInfo, declaration.plugins, appResourceLoader)
            .preprocessFile(declaration.vertFile)
        val fragCode = ShaderLoader(defs, fragDebugInfo, declaration.plugins, appResourceLoader)
            .preprocessFile(declaration.fragFile)
        return ShaderData(title, vertCode, fragCode, vertDebugInfo, fragDebugInfo)
    }

    private class ShaderLoader(private val defs: Set<String>, private val debugInfo: ShaderDebugInfo, private val plugins: Map<String, String>, private val appResourceLoader: ResourceLoader) {

        private val includedFnames: MutableSet<String> = mutableSetOf()

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
