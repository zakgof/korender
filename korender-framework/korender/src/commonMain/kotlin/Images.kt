package com.zakgof.korender

import com.zakgof.korender.math.ColorRGBA

interface Image {
    val width: Int
    val height: Int
    val format: Format

    fun pixel(x: Int, y: Int): ColorRGBA

    enum class Format {
        RGB,
        RGBA,
        Gray,
        Gray16
    }

}