package com.zakgof.korender.impl.font

import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.impl.engine.Retentionable
import com.zakgof.korender.impl.glgpu.GlGpuTexture

internal class Font(val gpuTexture: GlGpuTexture, val widths: FloatArray) : AutoCloseable {

    override fun close() = gpuTexture.close()

    fun textWidth(height: Int, text: String): Int =
        text.toCharArray()
            .map { widths[it.code] * height }
            .sum()
            .toInt()
}

internal class InternalRetentionableId (val id: String, override val retentionPolicy: RetentionPolicy ) : Retentionable {
    override fun equals(other: Any?): Boolean = (other is InternalRetentionableId && other.id == id)
    override fun hashCode(): Int = id.hashCode()
}
