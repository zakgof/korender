package com.zakgof.korender.impl.font

import com.zakgof.korender.Platform
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.impl.engine.ShaderDeclaration
import com.zakgof.korender.impl.material.Texturing
import com.zakgof.korender.impl.resourceBytes


internal object Fonts {

    val shaderDeclaration = ShaderDeclaration("shader/gui/font.vert", "shader/gui/font.frag")

    suspend fun load(fontResource: String, appResourceLoader: ResourceLoader): Font {
        val fontDef = Platform.loadFont(resourceBytes(appResourceLoader, fontResource))
        val gpuTexture = Texturing.create(fontResource, fontDef.await().image)
        return Font(gpuTexture, fontDef.await().widths)
    }
}