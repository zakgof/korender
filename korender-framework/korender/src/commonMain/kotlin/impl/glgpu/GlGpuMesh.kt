package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.impl.geometry.Attribute
import com.zakgof.korender.impl.gpu.GpuMesh
import java.nio.ByteBuffer

class GlGpuMesh(
    val attrs: List<Attribute>,
    val vertexSize: Int,
    isDynamic: Boolean = false,
    private val isLongIndex: Boolean = false
) : GpuMesh {

    private val vbHandle: Int = com.zakgof.korender.impl.gl.VGL15.glGenBuffers()
    private val ibHandle: Int = com.zakgof.korender.impl.gl.VGL15.glGenBuffers()
    private val usage: Int = if (isDynamic) com.zakgof.korender.impl.gl.VGL15.GL_DYNAMIC_DRAW else com.zakgof.korender.impl.gl.VGL15.GL_STATIC_DRAW

    private var vertices: Int = -1
    private var indices: Int = -1

    init {
        println("Create GPU mesh $vbHandle/$ibHandle")
    }

    override fun render() =
        com.zakgof.korender.impl.gl.VGL11.glDrawElements(
            com.zakgof.korender.impl.gl.VGL11.GL_TRIANGLES,
            indices,
            if (isLongIndex) com.zakgof.korender.impl.gl.VGL11.GL_UNSIGNED_INT else com.zakgof.korender.impl.gl.VGL11.GL_UNSIGNED_SHORT,
            0
        )

    override fun bind() {
        com.zakgof.korender.impl.gl.VGL15.glBindBuffer(com.zakgof.korender.impl.gl.VGL15.GL_ARRAY_BUFFER, vbHandle)
        com.zakgof.korender.impl.gl.VGL15.glBindBuffer(com.zakgof.korender.impl.gl.VGL15.GL_ELEMENT_ARRAY_BUFFER, ibHandle)
    }

    override fun update(vb: ByteBuffer, ib: ByteBuffer, vertices: Int, indices: Int) {
        this.vertices = vertices
        this.indices = indices
        bind()
        com.zakgof.korender.impl.gl.VGL15.glBufferData(com.zakgof.korender.impl.gl.VGL15.GL_ARRAY_BUFFER, vb.rewind() as ByteBuffer, usage)
        com.zakgof.korender.impl.gl.VGL15.glBufferData(com.zakgof.korender.impl.gl.VGL15.GL_ELEMENT_ARRAY_BUFFER, ib.rewind() as ByteBuffer, usage)
    }

    override fun close() {
        println("Destroy GPU mesh $vbHandle/$ibHandle")
        com.zakgof.korender.impl.gl.VGL15.glDeleteBuffers(vbHandle)
        com.zakgof.korender.impl.gl.VGL15.glDeleteBuffers(ibHandle)
    }
}