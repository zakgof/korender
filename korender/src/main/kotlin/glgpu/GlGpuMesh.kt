package com.zakgof.korender.glgpu

import com.zakgof.korender.geometry.Attribute
import com.zakgof.korender.gpu.GpuMesh
import gl.VGL11
import gl.VGL15
import java.nio.ByteBuffer

class GlGpuMesh(
    vb: ByteBuffer,
    ib: ByteBuffer,
    private val vertices: Int,
    private val indices: Int,
    val attrs: List<Attribute>,
    val vertexSize: Int,
    isDynamic: Boolean = false
) : GpuMesh {

    private val vbHandle: Int
    private val ibHandle: Int

    init {
        vbHandle = VGL15.glGenBuffers()
        ibHandle = VGL15.glGenBuffers()

        vb.rewind()
        ib.rewind()

        val usage: Int = if (isDynamic) VGL15.GL_DYNAMIC_DRAW else VGL15.GL_STATIC_DRAW // TODO : stream

        VGL15.glBindBuffer(VGL15.GL_ARRAY_BUFFER, vbHandle)
        VGL15.glBindBuffer(VGL15.GL_ELEMENT_ARRAY_BUFFER, ibHandle)
        VGL15.glBufferData(VGL15.GL_ARRAY_BUFFER, vb, usage)
        VGL15.glBufferData(VGL15.GL_ELEMENT_ARRAY_BUFFER, ib, usage)
    }

    fun render() {
        VGL11.glDrawElements(
            VGL11.GL_TRIANGLES,
            indices,
            if (vertices > 32767) VGL11.GL_UNSIGNED_INT else VGL11.GL_UNSIGNED_SHORT,
            0
        )
    }

    fun bind() {
        VGL15.glBindBuffer(VGL15.GL_ARRAY_BUFFER, vbHandle)
        VGL15.glBindBuffer(VGL15.GL_ELEMENT_ARRAY_BUFFER, ibHandle)
    }
}