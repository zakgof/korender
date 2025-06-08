package com.zakgof.korender.impl.font

import com.zakgof.korender.Platform
import com.zakgof.korender.impl.engine.Loader
import com.zakgof.korender.impl.engine.ShaderDeclaration
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import impl.engine.ImmediatelyFreeRetentionPolicy


internal object Fonts {

    val shaderDeclaration = ShaderDeclaration("!shader/gui/font.vert", "!shader/gui/font.frag", retentionPolicy = ImmediatelyFreeRetentionPolicy)

    fun load(fontResource: String, loader: Loader): Font? =
        loader.load(fontResource)?.let {
            loader.wait(fontResource) { Platform.loadFont(it) }
        }?.let {
            Font(GlGpuTexture(it.image), it.widths)
        }
}