package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.KorenderException
import com.zakgof.korender.impl.gl.VGL11
import com.zakgof.korender.impl.gl.VGL20
import com.zakgof.korender.impl.gpu.GpuMesh
import com.zakgof.korender.impl.gpu.GpuShader
import com.zakgof.korender.impl.gpu.GpuTexture
import com.zakgof.korender.impl.material.ShaderDebugInfo
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Mat3
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.uniforms.UniformSupplier
import java.nio.FloatBuffer
import java.nio.IntBuffer

class GlGpuShader(
    private val name: String,
    vertexShaderText: String,
    fragmentShaderText: String,
    vertDebugInfo: ShaderDebugInfo,
    fragDebugInfo: ShaderDebugInfo
) :
    GpuShader {
    private val programHandle: Int = VGL20.glCreateProgram()
    private val vertexShaderHandle: Int = VGL20.glCreateShader(VGL20.GL_VERTEX_SHADER)
    private val fragmentShaderHandle: Int = VGL20.glCreateShader(VGL20.GL_FRAGMENT_SHADER)
    private val uniformLocations: Map<String, Int>
    private val attributeLocations: Map<String, Int>

    init {

        VGL20.glShaderSource(vertexShaderHandle, vertexShaderText)
        VGL20.glCompileShader(vertexShaderHandle)

        VGL20.glShaderSource(fragmentShaderHandle, fragmentShaderText)
        VGL20.glCompileShader(fragmentShaderHandle)

        VGL20.glAttachShader(programHandle, vertexShaderHandle)
        VGL20.glAttachShader(programHandle, fragmentShaderHandle)

        VGL20.glLinkProgram(programHandle)
        VGL20.glValidateProgram(programHandle)

        val vertexLog: String = VGL20.glGetShaderInfoLog(vertexShaderHandle)
        if (vertexLog.isNotEmpty()) {
            System.err.println(
                "Vertex shader log [${vertDebugInfo.file}]\n\n" + vertDebugInfo.decorate(
                    vertexLog
                )
            )
        }

        val fragmentLog: String = VGL20.glGetShaderInfoLog(fragmentShaderHandle)
        if (fragmentLog.isNotEmpty()) {
            System.err.println(
                "Fragment shader log [${fragDebugInfo.file}]\n\n" + fragDebugInfo.decorate(
                    fragmentLog
                )
            )
        }

        val programLog: String = VGL20.glGetProgramInfoLog(programHandle)
        if (programLog.isNotEmpty()) {
            System.err.println("\nProgram log $name\n\n$programLog")
        }

        if (VGL20.glGetShaderi(vertexShaderHandle, VGL20.GL_COMPILE_STATUS) == 0) {
            throw RuntimeException("Vertex shader compilation failure")
        } else if (VGL20.glGetShaderi(fragmentShaderHandle, VGL20.GL_COMPILE_STATUS) == 0) {
            throw RuntimeException("Fragment shader compilation failure")
        } else if (VGL20.glGetProgrami(programHandle, VGL20.GL_LINK_STATUS) == 0) {
            throw RuntimeException("Program linking failure")
        } else if (VGL20.glGetProgrami(programHandle, VGL20.GL_VALIDATE_STATUS) == 0) {
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

    private fun fetchUniforms(): Map<String, Int> {
        val params: IntBuffer = BufferUtils.createIntBuffer(1)
        val type: IntBuffer = BufferUtils.createIntBuffer(1)

        val numUniforms = VGL20.glGetProgrami(programHandle, VGL20.GL_ACTIVE_UNIFORMS)
        return (0 until numUniforms).associate {
            val name: String = VGL20.glGetActiveUniform(
                programHandle,
                it,
                params.clear() as IntBuffer,
                type.clear() as IntBuffer
            )
            val location = VGL20.glGetUniformLocation(programHandle, name)
            name to location
        }
    }

    private fun fetchAttributes(): Map<String, Int> {
        val params: IntBuffer = BufferUtils.createIntBuffer(1)
        val type: IntBuffer = BufferUtils.createIntBuffer(1)

        val numAttributes = VGL20.glGetProgrami(programHandle, VGL20.GL_ACTIVE_ATTRIBUTES)
        return (0 until numAttributes).associate {
            val name: String = VGL20.glGetActiveAttrib(
                programHandle,
                it,
                params.clear() as IntBuffer,
                type.clear() as IntBuffer
            )
            val location = VGL20.glGetAttribLocation(programHandle, name);
            name to location
        }


    }

    override fun close() {
        println("Destroying GPU Shader [$name] : $programHandle")
        VGL20.glDeleteShader(vertexShaderHandle)
        VGL20.glDeleteShader(fragmentShaderHandle)
        VGL20.glDeleteProgram(programHandle)
    }

    override fun render(uniformSupplier: UniformSupplier, mesh: GpuMesh) {
        VGL20.glUseProgram(programHandle)
        bindAttrs(mesh as GlGpuMesh)
        bindUniforms(uniformSupplier)
        mesh.render()
        VGL20.glUseProgram(0)
    }

    private fun bindAttrs(mesh: GlGpuMesh) {
        mesh.bind()
        var offset = 0
        for (attr in mesh.attrs) {
            attributeLocations[attr.name]?.let {
                VGL20.glEnableVertexAttribArray(it)
                VGL20.glVertexAttribPointer(
                    it,
                    attr.size,
                    VGL11.GL_FLOAT,
                    false,
                    mesh.vertexSize,
                    offset
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
            if (bind(uniformValue, it.value, currentTexUnit))
                currentTexUnit++
        }
    }

    private fun bind(value: Any, location: Int, currentTexUnit: Int): Boolean {
        when (value) {
            is Int -> VGL20.glUniform1i(location, value)
            is Float -> VGL20.glUniform1f(location, value)
            is Vec2 -> VGL20.glUniform2f(location, value.x, value.y)
            is Vec3 -> VGL20.glUniform3f(location, value.x, value.y, value.z)
            is Color -> VGL20.glUniform4f(location, value.r, value.g, value.b, value.a)
            is Mat4 -> VGL20.glUniformMatrix4fv(
                location,
                false,
                value.asBuffer().rewind() as FloatBuffer
            )

            is Mat3 -> VGL20.glUniformMatrix3fv(
                location,
                false,
                value.asBuffer().rewind() as FloatBuffer
            )

            is GpuTexture -> {
                value.bind(currentTexUnit)
                VGL20.glUniform1i(location, currentTexUnit)
            }

            else -> {
                val uniformName = uniformLocations.entries.first { it.value == location }.key
                throw KorenderException("Unsupported uniform value $value of type ${value::class.java} for uniform $uniformName")
            }

        }
        return value is GpuTexture
    }

    override fun toString() = name
}