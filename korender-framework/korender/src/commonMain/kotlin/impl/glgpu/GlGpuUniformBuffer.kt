package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.KorenderException
import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.impl.buffer.put
import com.zakgof.korender.impl.gl.GL.glBindBuffer
import com.zakgof.korender.impl.gl.GL.glBindBufferBase
import com.zakgof.korender.impl.gl.GL.glBufferData
import com.zakgof.korender.impl.gl.GL.glDeleteBuffers
import com.zakgof.korender.impl.gl.GL.glGenBuffers
import com.zakgof.korender.impl.gl.GLConstants.GL_DYNAMIC_DRAW
import com.zakgof.korender.impl.gl.GLConstants.GL_UNIFORM_BUFFER
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Mat3
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

internal class GlGpuUniformBuffer(size: Int, private val offsets: Map<String, Int>) : AutoCloseable {

    private val ubo = glGenBuffers()
    private val uboBuffer = NativeByteBuffer(size)

    init {
        glBindBuffer(GL_UNIFORM_BUFFER, ubo);
        glBufferData(GL_UNIFORM_BUFFER, uboBuffer, GL_DYNAMIC_DRAW)
        println("Creating GPU UBO : $ubo")
    }

    fun bindShader(blockBinding: Int) = glBindBufferBase(GL_UNIFORM_BUFFER, blockBinding, ubo)

    fun populate(uniforms: (String) -> Any?, blockBinding: Int, materialName: String) {
        offsets.forEach {
            val uniformValue = requireNotNull(uniforms(it.key)) { "Material $materialName does not provide value for the blocked uniform ${it.key}" }
            populateUbo(it.key, uniformValue, it.value)
        }
        glBindBufferBase(GL_UNIFORM_BUFFER, blockBinding, ubo)
        glBindBuffer(GL_UNIFORM_BUFFER, ubo)
        glBufferData(GL_UNIFORM_BUFFER, uboBuffer.rewind(), GL_DYNAMIC_DRAW)
    }

    private fun populateUbo(name: String, value: Any, offset: Int) {
        uboBuffer.position(offset)
        when (value) {
            is Int -> uboBuffer.put(value)
            is Float -> uboBuffer.put(value)
            is Vec2 -> {
                uboBuffer.put(value.x)
                uboBuffer.put(value.y)
            }

            is Vec3 -> uboBuffer.put(value)
            is ColorRGB -> {
                uboBuffer.put(value.r)
                uboBuffer.put(value.g)
                uboBuffer.put(value.b)
            }

            is ColorRGBA -> {
                uboBuffer.put(value.r)
                uboBuffer.put(value.g)
                uboBuffer.put(value.b)
                uboBuffer.put(value.a)
            }

            is Mat3 -> uboBuffer.put(value.asArray()) // TODO: padding
            is Mat4 -> uboBuffer.put(value.asArray())

            is IntList -> value.values.forEach {
                uboBuffer.put(it)
            }

            is FloatList -> value.values.forEach {
                uboBuffer.put(it)
            }

            is Vec3List -> value.values.forEach {
                uboBuffer.put(it.x)
                uboBuffer.put(it.y)
                uboBuffer.put(it.z)
            }

            is Color3List -> value.values.forEach {
                uboBuffer.put(it.r)
                uboBuffer.put(it.g)
                uboBuffer.put(it.b)
            }

            is Color4List -> value.values.forEach {
                uboBuffer.put(it.r)
                uboBuffer.put(it.g)
                uboBuffer.put(it.b)
                uboBuffer.put(it.a)
            }

            is Mat4List -> value.matrices.forEach {
                uboBuffer.put(it.asArray())
            }

            else -> {
                throw KorenderException("Unsupported uniform value $value of type ${value::class} for uniform $name")
            }
        }
    }

    override fun close() {
        println("Destroying GPU UBO [$ubo]")
        glDeleteBuffers(ubo)
    }

}