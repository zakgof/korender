package com.zakgof.korender.context

import com.zakgof.korender.input.TouchEvent

interface KorenderContext {
    fun Frame(block: FrameContext.() -> Unit)
    fun OnTouch(handler: (TouchEvent) -> Unit)
}