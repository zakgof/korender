package com.zakgof.korender

import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.glgpu.BufferUtils
import com.zakgof.korender.gpu.GpuMesh
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer

object Meshes {

    fun create(vertexNumber: Int, indexNumber: Int, vararg attrs: Attribute, block: MeshBuilder.() -> Unit) =
        MeshBuilder(vertexNumber, indexNumber, attrs).apply(block)

    class MeshBuilder(
        private val vertexNumber: Int,
        private val indexNumber: Int,
        private val attrs: Array<out Attribute>
    ) {
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
            return gpu.createMesh(
                vertexBuffer.rewind(),
                indexBuffer.rewind(),
                vertexNumber,
                indexNumber,
                attrs.toList(),
                vertexSize,
                false
            )
        }
    }

    fun quad(halfSide: Float): MeshBuilder =
        create(4, 6, POS, NORMAL, TEX) {
            vertices(-halfSide, -halfSide, 0f, 0f, 0f, 1f, 0f, 0f)
            vertices(-halfSide, halfSide, 0f, 0f, 0f, 1f, 0f, 1f)
            vertices(halfSide, halfSide, 0f, 0f, 0f, 1f, 1f, 1f)
            vertices(halfSide, -halfSide, 0f, 0f, 0f, 1f, 1f, 0f)
            indices(0, 1, 2, 0, 2, 3)
        }
}