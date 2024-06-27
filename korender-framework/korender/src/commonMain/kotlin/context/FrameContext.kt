package com.zakgof.korender.context

interface FrameContext : PassContext {
    fun Shadow(block: ShadowContext.() -> Unit)
    fun Pass(block: PassContext.() -> Unit)
}