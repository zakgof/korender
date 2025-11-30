package com.zakgof.korender

import com.zakgof.korender.math.ColorRGBA

enum class PixelFormat(val bytes: Int) {
    RGB(3),
    RGBA(4),
    Gray(1),
    Gray16(2);
}

interface Image {

    val width: Int
    val height: Int
    val format: PixelFormat

    fun pixel(x: Int, y: Int): ColorRGBA

    fun setPixel(x: Int, y: Int, color: ColorRGBA)

    fun toTga(): ByteArray

    fun toRaw(): ByteArray
}

interface Image3D {

    val width: Int
    val height: Int
    val depth: Int
    val format: PixelFormat

    fun pixel(x: Int, y: Int, z: Int): ColorRGBA

    fun setPixel(x: Int, y: Int, z: Int, color: ColorRGBA)

    fun toRaw(): ByteArray
}