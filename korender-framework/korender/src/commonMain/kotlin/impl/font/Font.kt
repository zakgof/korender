package com.zakgof.korender.impl.font

import com.zakgof.korender.impl.gpu.GpuTexture

class Font(val gpuTexture: GpuTexture, val widths: FloatArray) : AutoCloseable {

    override fun close() = gpuTexture.close()

    fun textWidth(height: Int, text: String): Int =
        text.toCharArray()
            .map { widths[it.code] * height }
            .sum()
            .toInt()
}