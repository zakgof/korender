package com.zakgof.korender.math

data class Color(val r: Float, val g: Float, val b: Float) {

    constructor(rgb: Int) : this(
        rgb.shr(16).toFloat() / 255f,
        rgb.shr(8).and(0xFF).toFloat() / 255f,
        rgb.and(0xFF).toFloat() / 255f
    )

    companion object {
        val BLACK = Color(0f, 0f, 0f)
        val WHITE = Color(1f, 1f, 1f)
    }

    operator fun plus(c: Color): Color = Color(r + c.r, g + c.g, b + c.b)
    operator fun times(s: Float): Color = Color(s * r, s * g, s * b)
    operator fun times(s: Color): Color = Color(r * s.r, g * s.g, b * s.b)

    override fun toString(): String = "Color $r,$g,$b"

}