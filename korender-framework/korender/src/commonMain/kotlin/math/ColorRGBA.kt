package com.zakgof.korender.math

data class ColorRGBA(val r: Float, val g: Float, val b: Float, val a: Float) {

    constructor(rgba: Long) : this(
        rgba.shr(24).and(0xFF).toFloat() / 255f,
        rgba.shr(16).and(0xFF).toFloat() / 255f,
        rgba.shr(8).and(0xFF).toFloat() / 255f,
        rgba.and(0xFF).toFloat() / 255f
    )

    fun toRBG() = ColorRGB(r, g, b)

    companion object {
        val Transparent = ColorRGBA(0f, 0f, 0f, 0f)
        fun white(intensity: Float) = ColorRGBA(intensity, intensity, intensity, 1.0f)
        val Black = white(0f)
        val White = white(1f)
        val Blue = ColorRGBA(0f, 0f, 1f, 1f)
        val Green = ColorRGBA(0f, 1f, 0f, 1f)
        val Red = ColorRGBA(1f, 0f, 0f, 1f)
    }

    override fun toString(): String = "Color $r,$g,$b/$a"
}