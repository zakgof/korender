package com.zakgof.korender.impl.font

import com.zakgof.korender.impl.glgpu.GlGpuTexture

internal class Font(val gpuTexture: GlGpuTexture, val widths: FloatArray) : AutoCloseable {

    override fun close() = gpuTexture.close()

    fun textWidth(height: Int, text: String): Int =
        text.toCharArray()
            .map { widths[it.code] * height }
            .sum()
            .toInt()
}