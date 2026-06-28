package com.zakgof.korender.math

import kotlin.math.roundToInt

/**
 * RGBA color with 32-bit channels (each 0.0-1.0).
 * Represents color in linear RGB space with an alpha (transparency) channel.
 *
 * Example:
 * ```kotlin
 * val red = ColorRGBA(1.0f, 0.0f, 0.0f, 1.0f)
 * val semi_transparent = ColorRGBA(1.0f, 1.0f, 1.0f, 0.5f)
 * val from_hex = ColorRGBA(0xFF0000FF) // Red
 * ```
 *
 * @param r red channel (0.0 = no red, 1.0 = full red)
 * @param g green channel (0.0 = no green, 1.0 = full green)
 * @param b blue channel (0.0 = no blue, 1.0 = full blue)
 * @param a alpha channel (0.0 = fully transparent, 1.0 = fully opaque)
 */
data class ColorRGBA(val r: Float, val g: Float, val b: Float, val a: Float) {

    /**
     * Creates color from 32-bit hex RGBA value.
     * @param rgba 32-bit value where bits 24-31=R, 16-23=G, 8-15=B, 0-7=A (each 0-255)
     */
    constructor(rgba: Long) : this(
        rgba.shr(24).and(0xFF).toFloat() / 255f,
        rgba.shr(16).and(0xFF).toFloat() / 255f,
        rgba.shr(8).and(0xFF).toFloat() / 255f,
        rgba.and(0xFF).toFloat() / 255f
    )

    /**
     * Converts to RGB (discards alpha).
     * @return RGB color
     */
    fun toRGB() = ColorRGB(r, g, b)

    /**
     * Converts to 32-bit hex RGBA value.
     * @return 32-bit RGBA with channels in 0-255 range
     */
    fun toLong(): Long {
        fun pack(channel: Float): Long =
            (channel.coerceIn(0f, 1f) * 255f).roundToInt().toLong() and 0xFFL

        return (pack(r) shl 24) or (pack(g) shl 16) or (pack(b) shl 8) or pack(a)
    }

    companion object {
        /** Fully transparent black */
        val Transparent = ColorRGBA(0f, 0f, 0f, 0f)

        /**
         * Creates grayscale color.
         * @param intensity gray value (0.0 = black, 1.0 = white)
         * @return grayscale RGBA color (fully opaque)
         */
        fun white(intensity: Float) = ColorRGBA(intensity, intensity, intensity, 1.0f)

        /** Black (intensity 0.0) */
        val Black = white(0f)

        /** White (intensity 1.0) */
        val White = white(1f)

        /** Blue */
        val Blue = ColorRGBA(0f, 0f, 1f, 1f)

        /** Green */
        val Green = ColorRGBA(0f, 1f, 0f, 1f)

        /** Red */
        val Red = ColorRGBA(1f, 0f, 0f, 1f)
    }

    override fun toString(): String = "Color $r,$g,$b/$a"
}
