package com.zakgof.korender.impl.font

import com.zakgof.korender.Platform
import com.zakgof.korender.impl.context.NodeContext
import com.zakgof.korender.impl.engine.Loader
import com.zakgof.korender.impl.engine.NodeKeeper
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.image.InternalImage


internal object Fonts {

    fun load(fontResource: String, loader: Loader, nodeContext: NodeContext): Font? =
        loader.safeBytes(fontResource, nodeContext.resourceLoader) {
            loader.wait(fontResource) { Platform.loadFont(it) }
        }?.let {
            Font(GlGpuTexture(it.image), it.widths)
        }
}

internal class FontDef(val image: InternalImage, val widths: FloatArray)

internal class Font(val gpuTexture: GlGpuTexture, val widths: FloatArray) : AutoCloseable {

    override fun close() = gpuTexture.close()

    fun textWidth(height: Float, text: String): Float =
        text.toCharArray()
            .map { widths[it.code] * height }
            .sum()
}

internal class InternalFontDeclaration(val resource: String, override val nodeContext: NodeContext) : NodeKeeper {
    override fun equals(other: Any?): Boolean =
        (other is InternalFontDeclaration && other.resource == resource)

    override fun hashCode(): Int = resource.hashCode()
}
