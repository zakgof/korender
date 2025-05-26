package com.zakgof.korender.impl.font

import com.zakgof.korender.Platform
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.impl.engine.ShaderDeclaration
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.resourceBytes
import impl.engine.ImmediatelyFreeRetentionPolicy


internal object Fonts {

    val shaderDeclaration = ShaderDeclaration("!shader/gui/font.vert", "!shader/gui/font.frag", retentionPolicy = ImmediatelyFreeRetentionPolicy)

    suspend fun load(fontResource: String, appResourceLoader: ResourceLoader): Font {
        val fontDef = Platform.loadFont(resourceBytes(appResourceLoader, fontResource)).await()
        val gpuTexture = GlGpuTexture(fontResource, fontDef.image)
        return Font(gpuTexture, fontDef.widths)
    }
}