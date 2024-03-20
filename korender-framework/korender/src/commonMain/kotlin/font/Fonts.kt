package com.zakgof.korender.font

import com.zakgof.korender.declaration.ShaderDeclaration
import com.zakgof.korender.getPlatform
import com.zakgof.korender.gpu.Gpu
import com.zakgof.korender.material.Textures


object Fonts {

    val shaderDeclaration = ShaderDeclaration("font.vert", "font.frag", setOf())

    fun load(gpu: Gpu, fontResource: String): Font {
        val fontDef = getPlatform().loadFont(Fonts.javaClass.getResourceAsStream(fontResource)!!)
        val gpuTexture = Textures.create(fontDef.image).build(gpu)
        return Font(gpu, gpuTexture, fontDef.widths)
    }
}