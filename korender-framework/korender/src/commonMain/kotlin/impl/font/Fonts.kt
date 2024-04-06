package com.zakgof.korender.impl.font

import com.zakgof.korender.getPlatform
import com.zakgof.korender.impl.engine.ShaderDeclaration
import com.zakgof.korender.impl.gpu.Gpu
import com.zakgof.korender.impl.material.Texturing
import com.zakgof.korender.impl.resourceStream


internal object Fonts {

    val shaderDeclaration = ShaderDeclaration("font.vert", "font.frag", setOf())

    fun load(gpu: Gpu, fontResource: String): Font {
        val fontDef = getPlatform().loadFont(resourceStream(fontResource))
        val gpuTexture = Texturing.create(fontDef.image, gpu)
        return Font(gpuTexture, fontDef.widths)
    }
}