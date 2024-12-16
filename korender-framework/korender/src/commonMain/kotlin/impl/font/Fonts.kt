package com.zakgof.korender.impl.font

import com.zakgof.korender.getPlatform
import com.zakgof.korender.impl.engine.ShaderDeclaration
import com.zakgof.korender.impl.gpu.Gpu
import com.zakgof.korender.impl.material.Texturing
import com.zakgof.korender.impl.resourceBytes


internal object Fonts {

    val shaderDeclaration = ShaderDeclaration("gui/font.vert", "gui/font.frag")

    fun load(gpu: Gpu, fontResource: String): Font {
        val fontDef = getPlatform().loadFont(resourceBytes(fontResource))
        val gpuTexture = Texturing.create(fontResource, fontDef.image, gpu)
        return Font(gpuTexture, fontDef.widths)
    }
}