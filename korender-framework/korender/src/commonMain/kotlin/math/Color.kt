package com.zakgof.korender.math

data class Color(val a: Float, val r: Float, val g: Float, val b: Float) {

    constructor(argb: Long) : this(
        argb.shr(24).and(0xFF).toFloat() / 255f,
        argb.shr(16).and(0xFF).toFloat() / 255f,
        argb.shr(8).and(0xFF).toFloat() / 255f,
        argb.and(0xFF).toFloat() / 255f
    )

    companion object {
        fun white(intensity: Float) = Color(1f, intensity, intensity, intensity)
        val Transparent = Color(0f, 0f, 0f, 0f)
        val Black = white(0f)
        val White = white(1f)
        val Blue = Color(1f, 0f, 0f, 1f)
        val Green = Color(1f,0f, 1f, 0f)
        val Red = Color(1f,1f, 0f, 0f)
    }

    override fun toString(): String = "Color $a/$r,$g,$b"
}