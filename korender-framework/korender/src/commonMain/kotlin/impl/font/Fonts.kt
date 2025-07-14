package com.zakgof.korender.impl.font

import com.zakgof.korender.Platform
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.impl.engine.Loader
import com.zakgof.korender.impl.engine.Retentionable
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.image.InternalImage


internal object Fonts {

    fun load(fontResource: String, loader: Loader): Font? =
        loader.safeBytes(fontResource) {
            loader.wait(fontResource) { Platform.loadFont(it) }
        }?.let {
            Font(GlGpuTexture(it.image), it.widths)
        }
}

internal class FontDef(val image: InternalImage, val widths: FloatArray)

internal class Font(val gpuTexture: GlGpuTexture, val widths: FloatArray) : AutoCloseable {

    override fun close() = gpuTexture.close()

    fun textWidth(height: Int, text: String): Int =
        text.toCharArray()
            .map { widths[it.code] * height }
            .sum()
            .toInt()
}

internal class InternalFontDeclaration(val resource: String, override val retentionPolicy: RetentionPolicy) : Retentionable {
    override fun equals(other: Any?): Boolean =
        (other is InternalFontDeclaration && other.resource == resource)

    override fun hashCode(): Int = resource.hashCode()
}