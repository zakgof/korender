package com.zakgof.korender.math

data class ColorRGB(val r: Float, val g: Float, val b: Float) {

    constructor(rgb: Long) : this(
        rgb.shr(16).and(0xFF).toFloat() / 255f,
        rgb.shr(8).and(0xFF).toFloat() / 255f,
        rgb.and(0xFF).toFloat() / 255f
    )

    fun toRGBA(a: Float) = ColorRGBA(r, g, b, a)

    companion object {
        fun white(intensity: Float) = ColorRGB(intensity, intensity, intensity)
        val Black = white(0f)
        val White = white(1f)
        val Blue = ColorRGB(0f, 0f, 1f)
        val Green = ColorRGB(0f, 1f, 0f)
        val Red = ColorRGB(1f, 0f, 0f)
    }

    override fun toString(): String = "Color $r,$g,$b"
}