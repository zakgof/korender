package com.zakgof.korender.impl.material

import com.zakgof.korender.KorenderException
import com.zakgof.korender.impl.engine.ShaderDeclaration
import com.zakgof.korender.impl.gl.VGL11
import com.zakgof.korender.impl.gpu.Gpu
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.util.Scanner
import java.util.Stack
import java.util.regex.Pattern

internal object Shaders {

    val imageQuadDeclaration: ShaderDeclaration = ShaderDeclaration("gui/image.vert", "gui/image.frag")

    fun create(declaration: ShaderDeclaration, gpu: Gpu) =
        ShaderBuilder(declaration.vertFile, declaration.fragFile, declaration.defs, declaration.plugins).build(gpu)
}

private class ShaderBuilder(vertexShaderFile: String, fragmentShaderFile: String, defs: Set<String>, plugins: Map<String, String>) {

    private val defs: Set<String> = defs + VGL11.shaderEnv()
    private val title: String = "$vertexShaderFile/$fragmentShaderFile"
    private val vertDebugInfo: ShaderDebugInfo = ShaderDebugInfo(vertexShaderFile)
    private val fragDebugInfo: ShaderDebugInfo = ShaderDebugInfo(fragmentShaderFile)

    private val vertCode: String = preprocessFile(vertexShaderFile, this.defs, vertDebugInfo, plugins)
    private val fragCode: String = preprocessFile(fragmentShaderFile, this.defs, fragDebugInfo, plugins)

    private fun preprocessFile(fname: String, defs: Set<String>, debugInfo: ShaderDebugInfo, plugins: Map<String, String>): String {
        try {
            var inputStream = cpn("shader/$fname")
            if (inputStream == null) inputStream = cpn(fname)
            if (inputStream == null) throw KorenderException("Shader file not found: $fname")

            val text: String = inputStream.bufferedReader().use(BufferedReader::readText)
            val processedCode: String = preprocess(text, defs, fname, debugInfo, plugins)
            return processedCode
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun preprocess(code: String, defs: Set<String>, fname: String, debugInfo: ShaderDebugInfo, plugins: Map<String, String>): String {
        val sb = StringBuffer()
        val ifdefs = Stack<String>()
        val passes = Stack<Boolean>()
        passes.add(true)

        debugInfo.start(fname)

        Scanner(code).use { scanner ->
            while (scanner.hasNextLine()) {
                val line = scanner.nextLine()
                debugInfo.incSourceLine()

                val ifdefMatcher = Pattern.compile("#ifdef (.+)").matcher(line.trim { it <= ' ' })
                if (ifdefMatcher.matches()) {
                    val defname = ifdefMatcher.group(1)!!
                    ifdefs.push(defname)
                    passes.push(passes.peek() && defs.contains(defname))
                    continue
                }
                val ifndefMatcher = Pattern.compile("#ifndef (.+)").matcher(line.trim { it <= ' ' })
                if (ifndefMatcher.matches()) {
                    val defname = ifndefMatcher.group(1)!!
                    ifdefs.push("-$defname")
                    passes.push(passes.peek() && !defs.contains(defname))
                    continue
                }
                if (line.trim { it <= ' ' } == "#else") {
                    if (ifdefs.isEmpty())
                        throw KorenderException("Shader preprocessor error: #else without #if")
                    var inverting = ifdefs.pop()
                    passes.pop()
                    if (inverting.startsWith("!") || inverting.startsWith("+"))
                        throw KorenderException("Shader preprocessor error: repeated #else for $inverting")
                    if (inverting.startsWith("-")) {
                        inverting = inverting.substring(1)
                        ifdefs.push(inverting)
                        passes.push(passes.peek() && defs.contains(inverting))
                    } else {
                        ifdefs.push("!$inverting")
                        passes.push(passes.peek() && !defs.contains(inverting))
                    }
                    continue
                }
                if (line.trim { it <= ' ' } == "#endif") {
                    if (ifdefs.isEmpty())
                        throw KorenderException("Shader preprocessor error: #endif without #if")
                    ifdefs.pop()
                    passes.pop()
                    continue
                }

                if (!passes.peek())
                    continue

                val includeMatcher = Pattern.compile("#import \"(.+)\"").matcher(line.trim { it <= ' ' })
                if (includeMatcher.matches()) {
                    val include = includeMatcher.group(1)!!
                    val includeFname = includeToFile(include, plugins)
                    val includeContent = preprocessFile(includeFname, defs, debugInfo, plugins)
                    sb.append(includeContent)
                    continue
                }
                sb.append(line).append('\n')
                debugInfo.incDestLine(line)
            }
        }
        debugInfo.finish(fname)
        return sb.toString()
    }

    private fun includeToFile(include: String, plugins: Map<String, String>): String {
        return if (include.startsWith("$")) {
            plugins[include.substring(1)] ?: throw KorenderException("Cannot find shader plugin $include")
        } else {
            include
        }
    }

    fun cpn(filename: String?): InputStream? =
        ShaderBuilder::class.java.getClassLoader()!!.getResourceAsStream(filename)

    fun build(gpu: Gpu) =
        gpu.createShader(title, vertCode, fragCode, vertDebugInfo, fragDebugInfo)

}
