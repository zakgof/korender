package com.zakgof.korender.impl.gpu

import java.nio.ByteBuffer

interface GpuMesh : AutoCloseable {
    fun render()
    fun bind()
    fun update(vb: ByteBuffer, ib: ByteBuffer, vertices: Int, indices: Int)
}
