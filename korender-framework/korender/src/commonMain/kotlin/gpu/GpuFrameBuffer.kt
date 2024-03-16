package com.zakgof.korender.gpu

interface GpuFrameBuffer : AutoCloseable {

    val colorTexture: GpuTexture
    val depthTexture: GpuTexture?

    fun exec(block: () -> Unit)
}