package com.zakgof.korender.impl.image

import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.math.Color

internal interface Image {
    val width: Int
    val height: Int
    val bytes: NativeByteBuffer
    val format: GlGpuTexture.Format

    fun pixel(x: Int, y: Int) : Color
}