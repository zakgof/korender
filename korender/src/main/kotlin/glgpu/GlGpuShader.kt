package com.zakgof.korender.glgpu

import com.zakgof.korender.KorenderException
import com.zakgof.korender.gpu.GpuMesh
import com.zakgof.korender.gpu.GpuShader
import com.zakgof.korender.gpu.GpuTexture
import com.zakgof.korender.material.ShaderDebugInfo
import com.zakgof.korender.material.UniformSupplier
import com.zakgof.korender.math.Mat3
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Vec3
import gl.VGL11
import gl.VGL20
import java.nio.IntBuffer

class GlGpuShader(
    private val title: String,
    vertexShaderText: String,
    fragmentShaderText: String,
    vertDebugInfo: ShaderDebugInfo,
    fragDebugInfo: ShaderDebugInfo
) :
    GpuShader {
    private val programHandle: Int
    private val vertexShaderHandle: Int
    private val fragmentShaderHandle: Int
    private val uniformLocations: Map<String, Int>
    private val attributeLocations: Map<String, Int>

    init {
        programHandle = VGL20.glCreateProgram()
        vertexShaderHandle = VGL20.glCreateShader(VGL20.GL_VERTEX_SHADER)
        fragmentShaderHandle = VGL20.glCreateShader(VGL20.GL_FRAGMENT_SHADER)

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
            System.err.println("Vertex shader log [${vertDebugInfo.file}]\n\n" + vertDebugInfo.decorate(vertexLog))
        }

        val fragmentLog: String = VGL20.glGetShaderInfoLog(fragmentShaderHandle)
        if (fragmentLog.isNotEmpty()) {
            System.err.println("Fragment shader log [${fragDebugInfo.file}]\n\n" + fragDebugInfo.decorate(fragmentLog))
        }

        val programLog: String = VGL20.glGetProgramInfoLog(programHandle)
        if (programLog.isNotEmpty()) {
            System.err.println("\nProgram log $title\n\n$programLog")
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

        uniformLocations = fetchUniforms()
        attributeLocations = fetchAttributes()
    }

    private fun fetchUniforms(): Map<String, Int> {
        val params: IntBuffer = BufferUtils.createIntBuffer(1)
        val type: IntBuffer = BufferUtils.createIntBuffer(1)

        val numUniforms = VGL20.glGetProgrami(programHandle, VGL20.GL_ACTIVE_UNIFORMS)
        return (0 until numUniforms).map {
            val name: String = VGL20.glGetActiveUniform(programHandle, it, params.clear(), type.clear())
            val location = VGL20.glGetUniformLocation(programHandle, name);
            name to location
        }.toMap()
    }

    private fun fetchAttributes(): Map<String, Int> {
        val params: IntBuffer = BufferUtils.createIntBuffer(1)
        val type: IntBuffer = BufferUtils.createIntBuffer(1)

        val numAttributes = VGL20.glGetProgrami(programHandle, VGL20.GL_ACTIVE_ATTRIBUTES)
        return (0 until numAttributes).map {
            val name: String = VGL20.glGetActiveAttrib(programHandle, it, params.clear(), type.clear())
            val location = VGL20.glGetAttribLocation(programHandle, name);
            name to location
        }.toMap()


    }

    fun close() {
        // TODO Auto-generated method stub
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
                VGL20.glVertexAttribPointer(it, attr.size, VGL11.GL_FLOAT, false, mesh.vertexSize, offset)
            }
            offset += attr.size * 4 // TODO others than float
        }
    }

    private fun bindUniforms(uniformSupplier: UniformSupplier) {
        var currentTexUnit = 0
        uniformLocations.forEach {
            val uniformValue =
                requireNotNull(uniformSupplier[it.key]) { "Material does not provide value for the uniform ${it.key}" }
            if (bind(uniformValue, it.value, currentTexUnit))
                currentTexUnit++
        }
    }

    private fun bind(value: Any, location: Int, currentTexUnit: Int) : Boolean {
        when (value) {
            is Int -> VGL20.glUniform1i(location, value)
            is Float -> VGL20.glUniform1f(location, value)
            is Vec3 -> VGL20.glUniform3f(location, value.x, value.y, value.z)
            is Mat4 -> VGL20.glUniformMatrix4fv(location, false, value.asBuffer().rewind())
            is Mat3 -> VGL20.glUniformMatrix3fv(location, false, value.asBuffer().rewind())
            is GpuTexture -> {
                value.bind(currentTexUnit)
                VGL20.glUniform1i(location, currentTexUnit)
            }
            // is Mat2 -> VGL20.glUniformMatrix2fv(uniformLocationByName(uniform), false, value.rewind())
            // is Mat3 -> VGL20.glUniformMatrix3fv(uniformLocationByName(uniform), false, value.rewind())
            else -> throw KorenderException("Unsupported uniform type ${value::class.java}")
        }
        return value is GpuTexture
    }

    override fun toString() = title
}