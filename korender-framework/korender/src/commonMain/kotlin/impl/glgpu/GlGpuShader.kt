package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.KorenderException
import com.zakgof.korender.impl.engine.SkipRender
import com.zakgof.korender.impl.gl.GL.glAttachShader
import com.zakgof.korender.impl.gl.GL.glBindAttribLocation
import com.zakgof.korender.impl.gl.GL.glCompileShader
import com.zakgof.korender.impl.gl.GL.glCreateProgram
import com.zakgof.korender.impl.gl.GL.glCreateShader
import com.zakgof.korender.impl.gl.GL.glDeleteProgram
import com.zakgof.korender.impl.gl.GL.glDeleteShader
import com.zakgof.korender.impl.gl.GL.glGetActiveAttrib
import com.zakgof.korender.impl.gl.GL.glGetActiveUniform
import com.zakgof.korender.impl.gl.GL.glGetProgramInfoLog
import com.zakgof.korender.impl.gl.GL.glGetProgrami
import com.zakgof.korender.impl.gl.GL.glGetShaderInfoLog
import com.zakgof.korender.impl.gl.GL.glGetShaderi
import com.zakgof.korender.impl.gl.GL.glGetUniformLocation
import com.zakgof.korender.impl.gl.GL.glLinkProgram
import com.zakgof.korender.impl.gl.GL.glShaderSource
import com.zakgof.korender.impl.gl.GL.glUniform1f
import com.zakgof.korender.impl.gl.GL.glUniform1fv
import com.zakgof.korender.impl.gl.GL.glUniform1i
import com.zakgof.korender.impl.gl.GL.glUniform1iv
import com.zakgof.korender.impl.gl.GL.glUniform2f
import com.zakgof.korender.impl.gl.GL.glUniform3f
import com.zakgof.korender.impl.gl.GL.glUniform3fv
import com.zakgof.korender.impl.gl.GL.glUniform4f
import com.zakgof.korender.impl.gl.GL.glUniform4fv
import com.zakgof.korender.impl.gl.GL.glUniformMatrix3fv
import com.zakgof.korender.impl.gl.GL.glUniformMatrix4fv
import com.zakgof.korender.impl.gl.GL.glUseProgram
import com.zakgof.korender.impl.gl.GLConstants.GL_ACTIVE_ATTRIBUTES
import com.zakgof.korender.impl.gl.GLConstants.GL_ACTIVE_UNIFORMS
import com.zakgof.korender.impl.gl.GLConstants.GL_COMPILE_STATUS
import com.zakgof.korender.impl.gl.GLConstants.GL_FRAGMENT_SHADER
import com.zakgof.korender.impl.gl.GLConstants.GL_LINK_STATUS
import com.zakgof.korender.impl.gl.GLConstants.GL_VERTEX_SHADER
import com.zakgof.korender.impl.gl.GLUniformLocation
import com.zakgof.korender.impl.material.NotYetLoadedCubeTexture
import com.zakgof.korender.impl.material.NotYetLoadedTexture
import com.zakgof.korender.impl.material.ShaderDebugInfo
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Mat3
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

internal class GlGpuShader(
    private val name: String,
    vertexShaderText: String,
    fragmentShaderText: String,
    vertDebugInfo: ShaderDebugInfo,
    fragDebugInfo: ShaderDebugInfo,
    private val zeroTex: GlGpuTexture
) : AutoCloseable {
    private val programHandle = glCreateProgram()
    private val vertexShaderHandle = glCreateShader(GL_VERTEX_SHADER)
    private val fragmentShaderHandle = glCreateShader(GL_FRAGMENT_SHADER)
    private val uniformLocations: Map<String, GLUniformLocation>

    init {

        println("Creating GPU Shader [$name] : $programHandle")

        glShaderSource(vertexShaderHandle, vertexShaderText)
        glCompileShader(vertexShaderHandle)

        glShaderSource(fragmentShaderHandle, fragmentShaderText)
        glCompileShader(fragmentShaderHandle)

        glAttachShader(programHandle, vertexShaderHandle)
        glAttachShader(programHandle, fragmentShaderHandle)

        glLinkProgram(programHandle)

        var errorLog = "\n"
        val vertexLog: String = glGetShaderInfoLog(vertexShaderHandle)
        if (vertexLog.isNotEmpty()) {
            errorLog += "\n > Vertex shader log [${vertDebugInfo.file}]\n" + vertDebugInfo.decorate(vertexLog) + "\n"
        }

        val fragmentLog: String = glGetShaderInfoLog(fragmentShaderHandle)
        if (fragmentLog.isNotEmpty()) {
            errorLog += "\n >> Fragment shader log [${fragDebugInfo.file}]\n" + fragDebugInfo.decorate(fragmentLog) + "\n"
        }

        val programLog: String = glGetProgramInfoLog(programHandle)
        if (programLog.isNotEmpty()) {
            errorLog += "\n >> Program log [$name]\n${programLog.lines().joinToString("\n") { "       $it" }}\n"
        }

        if (glGetShaderi(vertexShaderHandle, GL_COMPILE_STATUS) == 0)
            throw KorenderException("Vertex shader compilation failure $errorLog")
        if (glGetShaderi(fragmentShaderHandle, GL_COMPILE_STATUS) == 0) {
            throw KorenderException("Fragment shader compilation failure $errorLog")
        } else if (glGetProgrami(programHandle, GL_LINK_STATUS) == 0) {
            throw KorenderException("Program linking failure $errorLog")
        } else if (vertexLog.isNotEmpty()) {
            throw KorenderException("Vertex shader compilation warnings $errorLog")
        } else if (fragmentLog.isNotEmpty()) {
            throw KorenderException("Fragment shader compilation warnings $errorLog")
        } else if (programLog.isNotEmpty()) {
            throw KorenderException("Program linking warnings $errorLog")
        }
        uniformLocations = fetchUniforms()
    }

    private fun fetchUniforms(): Map<String, GLUniformLocation> {
        val numUniforms = glGetProgrami(programHandle, GL_ACTIVE_UNIFORMS)
        return (0 until numUniforms).associate {
            val name: String = glGetActiveUniform(programHandle, it)
            val location = glGetUniformLocation(programHandle, name)
            name to location
        }
    }

    private fun fetchAttributes(): List<String> {
        val numAttributes = glGetProgrami(programHandle, GL_ACTIVE_ATTRIBUTES)
        return (0 until numAttributes).map {
            glGetActiveAttrib(programHandle, it)
        }
    }

    override fun close() {
        println("Destroying GPU Shader [$name] : $programHandle")
        glDeleteShader(vertexShaderHandle)
        glDeleteShader(fragmentShaderHandle)
        glDeleteProgram(programHandle)
    }

    fun render(uniforms: (String) -> Any?, mesh: GlGpuMesh) {
        glUseProgram(programHandle)
        bindUniforms(uniforms)
        bindAttrs(mesh)
        mesh.render()
        glUseProgram(null)
    }

    private fun bindAttrs(mesh: GlGpuMesh) {
        mesh.bind()
        mesh.attrs.forEach { attr ->
            glBindAttribLocation(programHandle, attr.location, attr.name)
        }
    }

    private fun bindUniforms(uniforms: (String) -> Any?) {
        var currentTexUnit = 0
        uniformLocations.forEach {
            val uniformValue =
                requireNotNull(uniforms(it.key)) { "Material ${toString()} does not provide value for the uniform ${it.key}" }
            currentTexUnit += bind(it.key, uniformValue, it.value, currentTexUnit)
        }
    }

    private fun bind(name: String, value: Any, location: GLUniformLocation, currentTexUnit: Int): Int {
        when (value) {
            is Int -> glUniform1i(location, value)
            is Float -> glUniform1f(location, value)
            is Vec2 -> glUniform2f(location, value.x, value.y)
            is Vec3 -> glUniform3f(location, value.x, value.y, value.z)
            is ColorRGB -> glUniform3f(location, value.r, value.g, value.b)
            is ColorRGBA -> glUniform4f(location, value.r, value.g, value.b, value.a)
            is Mat4 -> glUniformMatrix4fv(
                location, false, value.asArray()
            )

            is IntList -> if (value.values.isNotEmpty()) {
                glUniform1iv(location, *value.values.toIntArray())
            }

            is FloatList -> if (value.values.isNotEmpty()) {
                glUniform1fv(location, value.values.toFloatArray())
            }

            is Vec3List -> if (value.values.isNotEmpty()) {
                glUniform3fv(location, value.values.flatMap { listOf(it.x, it.y, it.z) }.toFloatArray())
            }

            is Color3List -> if (value.values.isNotEmpty()) {
                glUniform3fv(location, value.values.flatMap { listOf(it.r, it.g, it.b) }.toFloatArray())
            }

            is Color4List -> if (value.values.isNotEmpty()) {
                glUniform4fv(location, value.values.flatMap { listOf(it.r, it.g, it.b, it.a) }.toFloatArray())
            }

            is Mat3 -> glUniformMatrix3fv(
                location, false, value.asArray()
            )

            is Mat4List -> {
                if (value.matrices.isNotEmpty()) {
                    val fa = value.matrices.flatMap { it.asArray().asList() }.toFloatArray()
                    glUniformMatrix4fv(location, false, fa)
                }
            }

            is GlGpuTexture -> {
                // println("Bind texture unit $currentTexUnit to 2d texture $name")
                value.bind(currentTexUnit)
                glUniform1i(location, currentTexUnit)
            }

            is GlGpuCubeTexture -> {
                // println("Bind texture unit $currentTexUnit to cube texture $name")
                value.bind(currentTexUnit)
                glUniform1i(location, currentTexUnit)
            }

            is GlGpuTextureList -> {
                val units = (0 until value.totalNum)
                    .map {
                        val ctu = currentTexUnit + it
                        if (it < value.textures.size) {
                            value.textures[it].bind(ctu)
                        } else {
                            zeroTex.bind(ctu)
                        }
                        ctu
                    }
                // println("Bind texture unit $units to texture array $name")
                glUniform1iv(location, *units.toIntArray())
            }

            is NotYetLoadedTexture -> throw SkipRender

            is NotYetLoadedCubeTexture -> throw SkipRender

            else -> {
                throw KorenderException("Unsupported uniform value $value of type ${value::class} for uniform $name")
            }

        }
        // checkGlError("while setting uniform $name in shader $this")
        return when (value) {
            is GlGpuTexture -> 1
            is GlGpuCubeTexture -> 1
            is GlGpuTextureList -> value.totalNum
            else -> 0
        }
    }

    override fun toString() = name
}

internal data class IntList(val values: List<Int>)

internal data class FloatList(val values: List<Float>)

internal data class Mat4List(val matrices: List<Mat4>)

internal data class Vec3List(val values: List<Vec3>)

internal data class Color4List(val values: List<ColorRGBA>)

internal data class Color3List(val values: List<ColorRGB>)

internal data class GlGpuTextureList(val textures: List<GlGpuTexture>, val totalNum: Int)