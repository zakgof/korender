package com.zakgof.korender.context

import com.zakgof.korender.TextureDeclaration

interface RoiTexturesContext {
    fun RoiTexture(u: Float, v: Float, scale: Float, texture: TextureDeclaration)
}