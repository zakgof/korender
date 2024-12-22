package com.zakgof.korender.image

import com.zakgof.korender.buffer.Byter
import com.zakgof.korender.impl.gpu.GpuTexture
import com.zakgof.korender.math.Color

interface Image {
    val width: Int
    val height: Int
    val bytes: Byter
    val format: GpuTexture.Format

    fun pixel(x: Int, y: Int) : Color
}