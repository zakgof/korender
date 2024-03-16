package com.zakgof.korender.gpu

interface GpuTexture : AutoCloseable {
    fun bind(unit: Int)

    enum class Format {
        RGB,
        RGBA,
        Gray
    }
}