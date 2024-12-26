package com.zakgof.korender.image

import com.zakgof.korender.buffer.NativeByteBuffer
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.math.Color

interface Image {
    val width: Int
    val height: Int
    val bytes: NativeByteBuffer
    val format: GlGpuTexture.Format

    fun pixel(x: Int, y: Int) : Color
}