package com.zakgof.korender.impl.material

import com.zakgof.korender.impl.gpu.GpuTexture
import com.zakgof.korender.math.Color
import java.nio.ByteBuffer

interface Image {
    val width: Int
    val height: Int
    val bytes: ByteBuffer
    val format: GpuTexture.Format

    fun pixel(x: Int, y: Int) : Color
}