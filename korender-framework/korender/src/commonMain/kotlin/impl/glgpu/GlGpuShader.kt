package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.KorenderException
import com.zakgof.korender.buffer.BufferUtils
import com.zakgof.korender.buffer.Inter
import com.zakgof.korender.gl.GL.glAttachShader
import com.zakgof.korender.gl.GL.glCompileShader
import com.zakgof.korender.gl.GL.glCreateProgram
import com.zakgof.korender.gl.GL.glCreateShader
import com.zakgof.korender.gl.GL.glDeleteProgram
import com.zakgof.korender.gl.GL.glDeleteShader
import com.zakgof.korender.gl.GL.glEnableVertexAttribArray
import com.zakgof.korender.gl.GL.glGetActiveAttrib
import com.zakgof.korender.gl.GL.glGetActiveUniform
import com.zakgof.korender.gl.GL.glGetAttribLocation
import com.zakgof.korender.gl.GL.glGetProgramInfoLog
import com.zakgof.korender.gl.GL.glGetProgrami
import com.zakgof.korender.gl.GL.glGetShaderInfoLog
import com.zakgof.korender.gl.GL.glGetShaderi
import com.zakgof.korender.gl.GL.glGetUniformLocation
import com.zakgof.korender.gl.GL.glLinkProgram
import com.zakgof.korender.gl.GL.glShaderSource
import com.zakgof.korender.gl.GL.glUniform1f
import com.zakgof.korender.gl.GL.glUniform1i
import com.zakgof.korender.gl.GL.glUniform2f
import com.zakgof.korender.gl.GL.glUniform3f
import com.zakgof.korender.gl.GL.glUniform4f
import com.zakgof.korender.gl.GL.glUniformMatrix3fv
import com.zakgof.korender.gl.GL.glUniformMatrix4fv
import com.zakgof.korender.gl.GL.glUseProgram
import com.zakgof.korender.gl.GL.glValidateProgram
import com.zakgof.korender.gl.GL.glVertexAttribPointer
import com.zakgof.korender.gl.GLConstants.GL_ACTIVE_ATTRIBUTES
import com.zakgof.korender.gl.GLConstants.GL_ACTIVE_UNIFORMS
import com.zakgof.korender.gl.GLConstants.GL_COMPILE_STATUS
import com.zakgof.korender.gl.GLConstants.GL_FLOAT
import com.zakgof.korender.gl.GLConstants.GL_FRAGMENT_SHADER
import com.zakgof.korender.gl.GLConstants.GL_LINK_STATUS
import com.zakgof.korender.gl.GLConstants.GL_VALIDATE_STATUS
import com.zakgof.korender.gl.GLConstants.GL_VERTEX_SHADER
import com.zakgof.korender.gl.GLUniformLocation
import com.zakgof.korender.impl.gpu.GpuMesh
import com.zakgof.korender.impl.gpu.GpuShader
import com.zakgof.korender.impl.gpu.GpuTexture
import com.zakgof.korender.impl.material.NotYetLoadedTexture
import com.zakgof.korender.impl.material.ShaderDebugInfo
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Mat3
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.uniforms.UniformSupplier

class GlGpuShader(
    private val name: String,
    vertexShaderText: String,
    fragmentShaderText: String,
    vertDebugInfo: ShaderDebugInfo,
    fragDebugInfo: ShaderDebugInfo
) : GpuShader {
    private val programHandle = glCreateProgram()
    private val vertexShaderHandle = glCreateShader(GL_VERTEX_SHADER)
    private val fragmentShaderHandle = glCreateShader(GL_FRAGMENT_SHADER)
    private val uniformLocations: Map<String, GLUniformLocation>
    private val attributeLocations: Map<String, Int>

    init {

        glShaderSource(vertexShaderHandle, vertexShaderText)
        glCompileShader(vertexShaderHandle)

        glShaderSource(fragmentShaderHandle, fragmentShaderText)
        glCompileShader(fragmentShaderHandle)

        glAttachShader(programHandle, vertexShaderHandle)
        glAttachShader(programHandle, fragmentShaderHandle)

        glLinkProgram(programHandle)
        glValidateProgram(programHandle)

        val vertexLog: String = glGetShaderInfoLog(vertexShaderHandle)
        if (vertexLog.isNotEmpty()) {
            println(
                "Vertex shader log [${vertDebugInfo.file}]\n\n" + vertDebugInfo.decorate(
                    vertexLog
                )
            )
        }

        val fragmentLog: String = glGetShaderInfoLog(fragmentShaderHandle)
        if (fragmentLog.isNotEmpty()) {
            println(
                "Fragment shader log [${fragDebugInfo.file}]\n\n" + fragDebugInfo.decorate(
                    fragmentLog
                )
            )
        }

        val programLog: String = glGetProgramInfoLog(programHandle)
        if (programLog.isNotEmpty()) {
            println("\nProgram log $name\n\n$programLog")
        }

        if (glGetShaderi(vertexShaderHandle, GL_COMPILE_STATUS) == 0) {
            throw RuntimeException("Vertex shader compilation failure")
        } else if (glGetShaderi(fragmentShaderHandle, GL_COMPILE_STATUS) == 0) {
            throw RuntimeException("Fragment shader compilation failure")
        } else if (glGetProgrami(programHandle, GL_LINK_STATUS) == 0) {
            throw RuntimeException("Program linking failure")
        } else if (glGetProgrami(programHandle, GL_VALIDATE_STATUS) == 0) {
            throw RuntimeException("Program validation failure")
        } else if (vertexLog.isNotEmpty()) {
            throw RuntimeException("Vertex shader compilation warnings")
        } else if (fragmentLog.isNotEmpty()) {
            throw RuntimeException("Fragment shader compilation warnings")
        } else if (programLog.isNotEmpty()) {
            throw RuntimeException("Program linking warnings")
        }

        println("Creating GPU Shader [$name] : $programHandle")

        uniformLocations = fetchUniforms()
        attributeLocations = fetchAttributes()
    }

    private fun fetchUniforms(): Map<String, GLUniformLocation> {
        val params: Inter = BufferUtils.createIntBuffer(1)
        val type: Inter = BufferUtils.createIntBuffer(1)

        val numUniforms = glGetProgrami(programHandle, GL_ACTIVE_UNIFORMS)
        return (0 until numUniforms).associate {
            val name: String = glGetActiveUniform(
                programHandle, it, params.apply { clear() }, type.apply { clear() }
            )
            val location = glGetUniformLocation(programHandle, name)
            name to location
        }
    }

    private fun fetchAttributes(): Map<String, Int> {
        val params = BufferUtils.createIntBuffer(1)
        val type = BufferUtils.createIntBuffer(1)

        val numAttributes = glGetProgrami(programHandle, GL_ACTIVE_ATTRIBUTES)
        return (0 until numAttributes).associate {
            val name: String = glGetActiveAttrib(
                programHandle, it, params.apply { clear() }, type.apply { clear() }
            )
            val location = glGetAttribLocation(programHandle, name);
            name to location
        }

    }

    override fun close() {
        println("Destroying GPU Shader [$name] : $programHandle")
        glDeleteShader(vertexShaderHandle)
        glDeleteShader(fragmentShaderHandle)
        glDeleteProgram(programHandle)
    }

    override fun render(uniformSupplier: UniformSupplier, mesh: GpuMesh) {
        glUseProgram(programHandle)
        bindAttrs(mesh as GlGpuMesh)
        bindUniforms(uniformSupplier)
        mesh.render()
        glUseProgram(null)
    }

    private fun bindAttrs(mesh: GlGpuMesh) {
        mesh.bind()
        var offset = 0
        for (attr in mesh.attrs) {
            attributeLocations[attr.name]?.let {
                glEnableVertexAttribArray(it)
                glVertexAttribPointer(
                    it, attr.size, GL_FLOAT, false, mesh.vertexSize, offset
                )
            }
            offset += attr.size * 4 // TODO others than float
        }
    }

    private fun bindUniforms(uniformSupplier: UniformSupplier) {
        var currentTexUnit = 0
        uniformLocations.forEach {
            val uniformValue =
                requireNotNull(uniformSupplier[it.key]) { "Material ${toString()} does not provide value for the uniform ${it.key}" }
            if (bind(uniformValue, it.value, currentTexUnit)) currentTexUnit++
        }
    }

    private fun bind(value: Any, location: GLUniformLocation, currentTexUnit: Int): Boolean {
        when (value) {
            is Int -> glUniform1i(location, value)
            is Float -> glUniform1f(location, value)
            is Vec2 -> glUniform2f(location, value.x, value.y)
            is Vec3 -> glUniform3f(location, value.x, value.y, value.z)
            is Color -> glUniform4f(location, value.r, value.g, value.b, value.a)
            is Mat4 -> glUniformMatrix4fv(
                location, false, value.asBuffer().apply { rewind() }
            )

            is Mat3 -> glUniformMatrix3fv(
                location, false, value.asBuffer().apply { rewind() }
            )

            is GpuTexture -> {
                value.bind(currentTexUnit)
                glUniform1i(location, currentTexUnit)
            }

            is NotYetLoadedTexture -> {
                println("Warning: texture not yet loaded")
            }

            else -> {
                val uniformName = uniformLocations.entries.first { it.value == location }.key
                throw KorenderException("Unsupported uniform value $value of type ${value::class} for uniform $uniformName")
            }

        }
        return value is GpuTexture
    }

    override fun toString() = name
}