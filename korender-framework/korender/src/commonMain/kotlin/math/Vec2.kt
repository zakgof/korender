package com.zakgof.korender.math

import kotlin.math.sqrt
import kotlin.random.Random

/**
 * 2D vector with floating-point coordinates.
 * Used for texture coordinates, screen coordinates, and 2D operations.
 *
 * Example:
 * ```kotlin
 * val texCoord = Vec2(0.5f, 0.5f)
 * val scaled = texCoord * 2f
 * val direction = Vec2.Y
 * val distance = (texCoord - direction).length()
 * ```
 *
 * @param x X coordinate
 * @param y Y coordinate
 */
class Vec2(val x: Float, val y: Float) {

    companion object {
        /** Zero vector (0, 0) */
        val ZERO = Vec2(0f, 0f)

        /** Unit X vector (1, 0) */
        val X = Vec2(1f, 0f)

        /** Unit Y vector (0, 1) */
        val Y = Vec2(0f, 1f)

        /**
         * Creates a random unit vector seeded by value.
         * @param seed random seed
         * @return random normalized vector
         */
        fun random(seed: Int): Vec2 {
            val r = Random(seed)
            return Vec2(
                r.nextFloat() - 0.5f,
                r.nextFloat() - 0.5f,
            ).normalize()
        }

        /**
         * Creates a random unit vector using system random.
         * @return random normalized vector
         */
        fun random() = Vec2(
            Random.nextFloat() - 0.5f,
            Random.nextFloat() - 0.5f,
        ).normalize()
    }

    /** Negation: returns -this vector */
    operator fun unaryMinus(): Vec2 = Vec2(-x, -y)

    /** Dot product: this · other */
    operator fun times(s: Vec2) = x * s.x + y * s.y

    /** Scalar multiplication */
    operator fun times(a: Float) = Vec2(a * x, a * y)

    /** Per-component multiplication */
    fun multpercomp(s: Vec2) = Vec2(x * s.x, y * s.y)

    /** Vector addition */
    operator fun plus(s: Vec2) = Vec2(x + s.x, y + s.y)

    /** Vector subtraction */
    operator fun minus(s: Vec2) = Vec2(x - s.x, y - s.y)

    /** Length squared (faster than length() when only comparison is needed) */
    fun lengthSquared() = x * x + y * y

    /** Vector length (magnitude) */
    fun length() = sqrt(lengthSquared())

    /** Returns normalized vector (length = 1.0) */
    fun normalize(): Vec2 = this * (1f / length())

    override fun toString(): String = "($x, $y)"
}

