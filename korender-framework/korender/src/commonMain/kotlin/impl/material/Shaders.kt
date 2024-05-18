package com.zakgof.korender.impl.material

import com.zakgof.korender.impl.engine.CustomShaderDeclaration
import com.zakgof.korender.impl.gl.VGL11
import com.zakgof.korender.impl.gpu.Gpu
import com.zakgof.korender.impl.gpu.GpuShader
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.util.Scanner
import java.util.Stack
import java.util.regex.Pattern

internal object Shaders {

    val imageQuadDeclaration: CustomShaderDeclaration = CustomShaderDeclaration("gui/image.vert", "gui/image.frag", setOf())

    fun create(declaration: CustomShaderDeclaration, gpu: Gpu) =
        ShaderBuilder(declaration.vertFile, declaration.fragFile, declaration.defs).build(gpu)
    fun create(
        gpu: Gpu,
        vertexShaderFile: String,
        fragmentShaderFile: String,
        vararg defs: String
    ): GpuShader =
        ShaderBuilder(vertexShaderFile, fragmentShaderFile, setOf(*defs)).build(gpu)

    fun standard(gpu: Gpu, vararg defs: String): GpuShader =
        create(gpu, "standard.vert", "standard.frag", *defs)
}

private class ShaderBuilder(
    vertexShaderFile: String,
    fragmentShaderFile: String,
    defs: Set<String>
) {

    private var fragDebugInfo: ShaderDebugInfo
    private var vertDebugInfo: ShaderDebugInfo
    private var defs: Set<String>
    private var title: String
    private var fragCode: String
    private var vertCode: String

    init {
        val vfsplit: Array<String> =
            vertexShaderFile.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val ffsplit: Array<String> =
            fragmentShaderFile.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        this.title = vfsplit[vfsplit.size - 1] + "/" + ffsplit[ffsplit.size - 1]

        this.defs = defs + VGL11.shaderEnv()
        this.vertDebugInfo = ShaderDebugInfo(vertexShaderFile)
        this.fragDebugInfo = ShaderDebugInfo(fragmentShaderFile)
        this.vertCode = preprocessFile("", vertexShaderFile, this.defs, vertDebugInfo)
        this.fragCode = preprocessFile("", fragmentShaderFile, this.defs, fragDebugInfo)
    }

    private fun preprocessFile(
        startDir: String,
        fname: String,
        defs: Set<String>,
        debugInfo: ShaderDebugInfo
    ): String {
        var currDir = startDir
        try {
            var inputStream: InputStream? = cpn(currDir + fname)
            currDir += fname
            val c = currDir.lastIndexOf('/')
            currDir = currDir.substring(0, c + 1)

            if (inputStream == null) inputStream = cpn("shader/$fname")
            if (inputStream == null) inputStream = cpn(fname)

            if (inputStream == null) throw IOException("Shader file not found: $fname")
            val text: String = inputStream.bufferedReader().use(BufferedReader::readText)
            val processedCode: String = preprocess(currDir, text, defs, fname, debugInfo)
            return processedCode
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun preprocess(
        currDir: String,
        code: String,
        defs: Set<String>,
        fname: String,
        debugInfo: ShaderDebugInfo
    ): String {
        val sb = StringBuffer()
        val ifdefs = Stack<String>()
        val passes = Stack<Boolean>()
        passes.add(true)

        debugInfo.start(fname)

        Scanner(code).use { scanner ->
            while (scanner.hasNextLine()) {
                val line = scanner.nextLine()
                debugInfo.incSourceLine()

                val ifdefMatcher =
                    Pattern.compile("#ifdef (.+)").matcher(line.trim { it <= ' ' })
                if (ifdefMatcher.matches()) {
                    val defname = ifdefMatcher.group(1)!!
                    ifdefs.push(defname)
                    passes.push(passes.peek() && defs.contains(defname))
                    continue
                }
                val ifndefMatcher =
                    Pattern.compile("#ifndef (.+)").matcher(line.trim { it <= ' ' })
                if (ifndefMatcher.matches()) {
                    val defname = ifndefMatcher.group(1)!!
                    ifdefs.push("-$defname")
                    passes.push(passes.peek() && !defs.contains(defname))
                    continue
                }
                if (line.trim { it <= ' ' } == "#else") {
                    if (ifdefs.isEmpty()) throw java.lang.RuntimeException("Shader preprocessor error: #else without #if")
                    var inverting = ifdefs.pop()
                    passes.pop()
                    if (inverting.startsWith("!") || inverting.startsWith("+")) throw java.lang.RuntimeException(
                        "Shader preprocessor error: repeated #else for $inverting"
                    )
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
                    if (ifdefs.isEmpty()) throw java.lang.RuntimeException("Shader preprocessor error: #endif without #if")
                    ifdefs.pop()
                    passes.pop()
                    continue
                }

                if (!passes.peek()) continue

                val includeMatcher =
                    Pattern.compile("#import \"(.+)\"").matcher(line)
                if (includeMatcher.matches()) {
                    val inclfname = includeMatcher.group(1)!!
                    val includeContent: String =
                        preprocessFile(currDir, inclfname, defs, debugInfo)
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

    fun cpn(filename: String?): InputStream? =
        ShaderBuilder::class.java.getClassLoader()!!.getResourceAsStream(filename)

    fun build(gpu: Gpu) =
        gpu.createShader(title, vertCode, fragCode, vertDebugInfo, fragDebugInfo)

}
