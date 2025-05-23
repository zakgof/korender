package com.zakgof.korender.impl.font

import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import impl.engine.Retentionable

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

internal class InternalFontMeshDeclaration(val id: String, override val retentionPolicy: RetentionPolicy) : Retentionable {
    override fun equals(other: Any?): Boolean =
        (other is InternalFontMeshDeclaration && other.id == id)

    override fun hashCode(): Int = id.hashCode()
}