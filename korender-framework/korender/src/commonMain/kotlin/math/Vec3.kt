package com.zakgof.korender.math

import com.zakgof.korender.KorenderException
import kotlin.math.acos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * 3D vector with floating-point coordinates.
 * Used for positions, directions, normals, and any 3D mathematical operations.
 *
 * Example:
 * ```kotlin
 * val position = Vec3(1f, 2f, 3f)
 * val direction = Vec3.Z
 * val scaled = direction * 5f
 * val dotProduct = position dot direction
 * val crossProduct = position cross direction
 * ```
 *
 * @param x X coordinate
 * @param y Y coordinate
 * @param z Z coordinate
 */
class Vec3(val x: Float, val y: Float, val z: Float) {

    companion object {
        /** Zero vector (0, 0, 0) */
        val ZERO = Vec3(0f, 0f, 0f)

        /** Unit X vector (1, 0, 0) */
        val X = Vec3(1f, 0f, 0f)

        /** Unit Y vector (0, 1, 0) */
        val Y = Vec3(0f, 1f, 0f)

        /** Unit Z vector (0, 0, 1) */
        val Z = Vec3(0f, 0f, 1f)

        /**
         * Creates a random unit vector seeded by value.
         * @param seed random seed
         * @return random normalized vector
         */
        fun random(seed: Int): Vec3 {
            val r = Random(seed)
            return Vec3(
                r.nextFloat() - 0.5f,
                r.nextFloat() - 0.5f,
                r.nextFloat() - 0.5f,
            ).normalize()
        }

        /**
         * Creates a random unit vector using system random.
         * @return random normalized vector
         */
        fun random() = Vec3(
            Random.nextFloat() - 0.5f,
            Random.nextFloat() - 0.5f,
            Random.nextFloat() - 0.5f,
        ).normalize()

        /**
         * Gets unit vector along specified axis.
         * @param axis 0=X, 1=Y, 2=Z
         * @return unit vector along the axis
         * @throws KorenderException if axis is not 0, 1, or 2
         */
        fun unit(axis: Int) = when (axis) {
            0 -> X
            1 -> Y
            2 -> Z
            else -> throw KorenderException("Unknown axis index $axis")
        }

        fun slerp(a: Vec3, b: Vec3, t: Float): Vec3 {
            val dot = a.dot(b).coerceIn(-1f, 1f)
            val theta = acos(dot)
            val sinTheta = sin(theta)
            if (sinTheta < 1e-4) {
                return (a * (1f - t) + b * t).normalize()
            }
            val w1 = (sin((1f - t) * theta) / sinTheta)
            val w2 = (sin(t * theta) / sinTheta)
            return a * w1 + b * w2
        }
    }

    /** Negation: returns -this vector */
    operator fun unaryMinus(): Vec3 = Vec3(-x, -y, -z)

    /** Dot product: this · other */
    infix fun dot(s: Vec3) = x * s.x + y * s.y + z * s.z

    /** Dot product (operator version): this · other */
    operator fun times(s: Vec3) = x * s.x + y * s.y + z * s.z

    /** Cross product: this × other */
    infix fun cross(s: Vec3) = Vec3(y * s.z - z * s.y, z * s.x - x * s.z, x * s.y - y * s.x)

    /** Cross product (operator version): this × other */
    operator fun rem(s: Vec3) = Vec3(y * s.z - z * s.y, z * s.x - x * s.z, x * s.y - y * s.x)

    /** Scalar multiplication */
    operator fun times(a: Float) = Vec3(a * x, a * y, a * z)

    /** Scalar division */
    operator fun div(a: Float) = Vec3(x / a, y / a, z / a)

    /** Per-component multiplication */
    infix fun multpercomp(s: Vec3) = Vec3(x * s.x, y * s.y, z * s.z)

    /** Per-component division */
    infix fun divpercomp(s: Vec3) = Vec3(x / s.x, y / s.y, z / s.z)

    /** Vector addition */
    operator fun plus(s: Vec3) = Vec3(x + s.x, y + s.y, z + s.z)

    /** Vector subtraction */
    operator fun minus(s: Vec3) = Vec3(x - s.x, y - s.y, z - s.z)

    /** Length squared (faster than length() when only comparison is needed) */
    fun lengthSquared() = x * x + y * y + z * z

    /** Vector length (magnitude) */
    fun length() = sqrt(lengthSquared())

    /** Distance to another vector */
    infix fun distanceTo(a: Vec3) = (this - a).length()

    /** Returns normalized vector (length = 1.0) */
    fun normalize(): Vec3 = this * (1f / length())

    override fun toString(): String = "($x, $y, $z)"
}

/**
 * Convenience extension to create Vec3 from Int (as X component).
 * @receiver the value to use as X coordinate
 * @return Vec3(value, 0, 0)
 */
val Int.x: Vec3
    get() = Vec3(this.toFloat(), 0f, 0f)

/**
 * Convenience extension to create Vec3 from Int (as Y component).
 * @receiver the value to use as Y coordinate
 * @return Vec3(0, value, 0)
 */
val Int.y: Vec3
    get() = Vec3(0f, this.toFloat(), 0f)

/**
 * Convenience extension to create Vec3 from Int (as Z component).
 * @receiver the value to use as Z coordinate
 * @return Vec3(0, 0, value)
 */
val Int.z: Vec3
    get() = Vec3(0f, 0f, this.toFloat())

/**
 * Convenience extension to create Vec3 from Float (as X component).
 * @receiver the value to use as X coordinate
 * @return Vec3(value, 0, 0)
 */
val Float.x: Vec3
    get() = Vec3(this, 0f, 0f)

/**
 * Convenience extension to create Vec3 from Float (as Y component).
 * @receiver the value to use as Y coordinate
 * @return Vec3(0, value, 0)
 */
val Float.y: Vec3
    get() = Vec3(0f, this, 0f)

/**
 * Convenience extension to create Vec3 from Float (as Z component).
 * @receiver the value to use as Z coordinate
 * @return Vec3(0, 0, value)
 */
val Float.z: Vec3
    get() = Vec3(0f, 0f, this)
