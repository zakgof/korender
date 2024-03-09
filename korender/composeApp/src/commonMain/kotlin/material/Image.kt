package com.zakgof.korender.material

import com.zakgof.korender.gpu.GpuTexture
import java.nio.ByteBuffer

interface Image {
    val width: Int
    val height: Int
    val bytes: ByteBuffer
    val format: GpuTexture.Format
}