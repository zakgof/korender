package com.zakgof.korender.math

/**
 * RGB color with 32-bit floating-point channels (each 0.0-1.0).
 * Represents color in linear RGB space without an alpha channel.
 *
 * Example:
 * ```kotlin
 * val red = ColorRGB(1.0f, 0.0f, 0.0f)
 * val white = ColorRGB.White
 * val gray = ColorRGB.white(0.5f)
 * val from_hex = ColorRGB(0xFF0000) // Red
 * ```
 *
 * @param r red channel (0.0 = no red, 1.0 = full red)
 * @param g green channel (0.0 = no green, 1.0 = full green)
 * @param b blue channel (0.0 = no blue, 1.0 = full blue)
 */
data class ColorRGB(val r: Float, val g: Float, val b: Float) {

    /**
     * Creates color from 24-bit hex RGB value.
     * @param rgb 24-bit value where bits 16-23=R, 8-15=G, 0-7=B (each 0-255)
     */
    constructor(rgb: Long) : this(
        rgb.shr(16).and(0xFF).toFloat() / 255f,
        rgb.shr(8).and(0xFF).toFloat() / 255f,
        rgb.and(0xFF).toFloat() / 255f
    )

    /**
     * Scalar multiplication of color (brightens or darkens).
     * @param ratio multiplier for all channels
     * @return new color with scaled values
     */
    operator fun times(ratio: Float) = ColorRGB(r * ratio, g * ratio, b * ratio)

    /**
     * Color addition (mixes colors).
     * @param other color to add
     * @return new color with added values (clamped to 1.0)
     */
    operator fun plus(other: ColorRGB) = ColorRGB(r + other.r, g + other.g, b + other.g)

    /**
     * Converts to RGBA by adding an alpha channel.
     * @param a alpha value
     * @return RGBA color
     */
    fun toRGBA(a: Float) = ColorRGBA(r, g, b, a)

    companion object {
        /**
         * Creates grayscale color.
         * @param intensity gray value (0.0 = black, 1.0 = white)
         * @return grayscale RGB color
         */
        fun white(intensity: Float) = ColorRGB(intensity, intensity, intensity)

        /** Black (intensity 0.0) */
        val Black = white(0f)

        /** White (intensity 1.0) */
        val White = white(1f)

        /** Blue */
        val Blue = ColorRGB(0f, 0f, 1f)

        /** Green */
        val Green = ColorRGB(0f, 1f, 0f)

        /** Red */
        val Red = ColorRGB(1f, 0f, 0f)
    }

    override fun toString(): String = "Color $r,$g,$b"
}