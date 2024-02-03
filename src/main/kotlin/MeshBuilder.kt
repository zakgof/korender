package com.zakgof.korender

import com.zakgof.korender.glgpu.Attribute
import com.zakgof.korender.glgpu.BufferUtils
import com.zakgof.korender.gpu.GpuMesh
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer

fun mesh(vertexNumber: Int, indexNumber: Int, vararg attrs: Attribute, block: MeshBuilder.() -> Unit) =
    MeshBuilder(vertexNumber, indexNumber, attrs).apply(block)

class MeshBuilder(private val vertexNumber: Int, private val indexNumber: Int, private val attrs: Array<out Attribute>) {

    private val vertexBuffer: ByteBuffer
    private var floatVertexBuffer: FloatBuffer

    private val indexBuffer: ByteBuffer
    private val indexIntBuffer: IntBuffer?
    private val indexShortBuffer: ShortBuffer?
    private var isLongIndex: Boolean

    private val vertexSize: Int

    init {
        this.vertexSize = attrs.sumOf { it.size } * 4
        this.vertexBuffer = BufferUtils.createByteBuffer(vertexNumber * vertexSize)
        this.floatVertexBuffer = vertexBuffer.asFloatBuffer()

        this.isLongIndex = vertexNumber > 32767 // TODO : doesn't work for shared bulk buffer !

        this.indexBuffer = BufferUtils.createByteBuffer(indexNumber * (if (isLongIndex) 4 else 2))

        this.indexShortBuffer = if (isLongIndex) null else indexBuffer.asShortBuffer()
        this.indexIntBuffer = if (isLongIndex) indexBuffer.asIntBuffer() else null
    }

    fun vertices(vararg values: Float) {
        floatVertexBuffer.put(values)
    }

    fun indices(vararg values: Int) {
        for (value in values) {
            if (isLongIndex)
                indexIntBuffer!!.put(value)
            else
                indexShortBuffer!!.put(value.toShort())
        }
    }

    fun build(gpu: Gpu): GpuMesh {
        // TODO: validate vb/ib full
        return gpu.createMesh( vertexBuffer.rewind(), indexBuffer.rewind(), vertexNumber, indexNumber, attrs.toList(), vertexSize, false )
    }
}