package com.zakgof.korender.impl.gpu

interface GpuTexture : AutoCloseable {
    fun bind(unit: Int)

    enum class Format {
        RGB,
        RGBA,
        Gray,
        Gray16
    }
}