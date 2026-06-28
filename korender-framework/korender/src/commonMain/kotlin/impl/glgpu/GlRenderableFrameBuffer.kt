package com.zakgof.korender.impl.glgpu

internal fun interface GlRenderableFrameBuffer {
    fun exec(block: () -> Unit)
}

internal fun renderTo(target: GlRenderableFrameBuffer?, block: () -> Unit) =
    if (target == null) block() else target.exec(block)
