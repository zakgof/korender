package com.zakgof.korender.gpu

interface GpuFrameBuffer {

    val colorTexture: GpuTexture
    val depthTexture: GpuTexture?

    fun exec(block: () -> Unit)
}