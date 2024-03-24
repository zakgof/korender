package com.zakgof.korender.impl.font

import com.zakgof.korender.declaration.ShaderDeclaration
import com.zakgof.korender.impl.gpu.Gpu
import com.zakgof.korender.getPlatform
import com.zakgof.korender.impl.material.Texturing


object Fonts {

    val shaderDeclaration = ShaderDeclaration("font.vert", "font.frag", setOf())

    fun load(gpu: Gpu, fontResource: String): Font {
        val fontDef = getPlatform().loadFont(Fonts.javaClass.getResourceAsStream(fontResource)!!)
        val gpuTexture = Texturing.create(fontDef.image).build(gpu)
        return Font(gpuTexture, fontDef.widths)
    }
}