package com.zakgof.korender.math

data class Color(val a: Float, val r: Float, val g: Float, val b: Float) {

    constructor(argb: Long) : this(
        argb.shr(24).and(0xFF).toFloat() / 255f,
        argb.shr(16).and(0xFF).toFloat() / 255f,
        argb.shr(8).and(0xFF).toFloat() / 255f,
        argb.and(0xFF).toFloat() / 255f
    )

    companion object {
        val TRANSPARENT = Color(0f, 0f, 0f, 0f)
        val BLACK = Color(1f, 0f, 0f, 0f)
        val WHITE = Color(1f,1f, 1f, 1f)
    }

    override fun toString(): String = "Color $a/$r,$g,$b"
}