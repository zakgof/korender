package com.zakgof.korender.gpu

import java.nio.ByteBuffer

interface GpuMesh {
    fun render()
    fun bind()
    fun update(vb: ByteBuffer, ib: ByteBuffer, vertices: Int, indices: Int)
}
