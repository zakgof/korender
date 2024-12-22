package com.zakgof.korender.impl.gpu

import com.zakgof.korender.buffer.BufferData
import com.zakgof.korender.buffer.Floater

interface GpuMesh : AutoCloseable {
    fun render()
    fun bind()
    fun update(vb: Floater, ib: BufferData<out Any>, vertices: Int, indices: Int)
}
