package com.zakgof.korender.context

import com.zakgof.korender.input.TouchEvent
import com.zakgof.korender.material.TextureDeclaration
import com.zakgof.korender.material.TextureFilter
import com.zakgof.korender.material.TextureWrap

interface KorenderContext {
    fun Frame(block: FrameContext.() -> Unit)
    fun OnTouch(handler: (TouchEvent) -> Unit)

    fun texture(
        textureResource: String,
        filter: TextureFilter = TextureFilter.MipMapLinearLinear,
        wrap: TextureWrap = TextureWrap.Repeat,
        aniso: Int = 1024
    ): TextureDeclaration
}