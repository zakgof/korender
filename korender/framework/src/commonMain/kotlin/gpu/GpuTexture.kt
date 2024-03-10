package com.zakgof.korender.gpu

interface GpuTexture {
    fun bind(unit: Int)

    enum class Format {
        RGB,
        RGBA,
        Gray
    }
}