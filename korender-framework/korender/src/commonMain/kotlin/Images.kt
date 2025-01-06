package com.zakgof.korender

import com.zakgof.korender.math.Color

interface Image {
    val width: Int
    val height: Int
    val format: Format

    fun pixel(x: Int, y: Int): Color

    enum class Format {
        RGB,
        RGBA,
        Gray,
        Gray16
    }

}