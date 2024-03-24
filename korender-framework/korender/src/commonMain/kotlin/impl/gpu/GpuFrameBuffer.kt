package com.zakgof.korender.impl.gpu

interface GpuFrameBuffer : AutoCloseable {

    val colorTexture: GpuTexture
    val depthTexture: GpuTexture?

    fun exec(block: () -> Unit)
}