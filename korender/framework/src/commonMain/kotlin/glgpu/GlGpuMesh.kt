package com.zakgof.korender.glgpu

import com.zakgof.korender.geometry.Attribute
import com.zakgof.korender.gl.VGL11
import com.zakgof.korender.gl.VGL15
import com.zakgof.korender.gpu.GpuMesh
import java.nio.ByteBuffer

class GlGpuMesh(
    val attrs: List<Attribute>,
    val vertexSize: Int,
    isDynamic: Boolean = false
) : GpuMesh {

    private val vbHandle: Int = VGL15.glGenBuffers()
    private val ibHandle: Int = VGL15.glGenBuffers()
    private val usage: Int = if (isDynamic) VGL15.GL_DYNAMIC_DRAW else VGL15.GL_STATIC_DRAW

    private var vertices: Int = -1
    private var indices: Int  = -1

    override fun render() {

        VGL11.glDrawElements(
            VGL11.GL_TRIANGLES,
            indices,
            if (vertices > 32767) VGL11.GL_UNSIGNED_INT else VGL11.GL_UNSIGNED_SHORT,
            0
        )
    }

    override fun bind() {
        VGL15.glBindBuffer(VGL15.GL_ARRAY_BUFFER, vbHandle)
        VGL15.glBindBuffer(VGL15.GL_ELEMENT_ARRAY_BUFFER, ibHandle)
    }

    override fun update(vb: ByteBuffer, ib: ByteBuffer, vertices: Int, indices: Int) {
        this.vertices = vertices
        this.indices = indices
        bind()
        VGL15.glBufferData(VGL15.GL_ARRAY_BUFFER, vb.rewind() as ByteBuffer, usage)
        VGL15.glBufferData(VGL15.GL_ELEMENT_ARRAY_BUFFER, ib.rewind() as ByteBuffer, usage)
    }
}