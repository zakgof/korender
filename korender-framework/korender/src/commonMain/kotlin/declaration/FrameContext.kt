package com.zakgof.korender.declaration

interface FrameContext : PassContext {
    fun Shadow(block: ShadowContext.() -> Unit)
    fun Pass(block: PassContext.() -> Unit)
}