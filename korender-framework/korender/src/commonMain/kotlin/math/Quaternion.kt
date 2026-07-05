package com.zakgof.korender.math

import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Quaternion for 3D rotations. Represented as (w, r) where w is scalar and r is vector part.
 * Use companion object factory methods to create rotations.
 */
class Quaternion(val w: Float, val r: Vec3) {

    companion object {
        /**
         * Creates a quaternion from axis-angle representation.
         * @param axis normalized rotation axis
         * @param angle rotation angle in radians
         * @return unit quaternion representing the rotation
         */
        fun fromAxisAngle(axis: Vec3, angle: Float): Quaternion =
            Quaternion(cos(angle * 0.5f), axis * sin(angle * 0.5f))

        /**
         * Creates the shortest arc quaternion between two vectors.
         * @param v1 start vector (normalized)
         * @param v2 end vector (normalized)
         * @return quaternion rotating v1 to v2 via shortest path
         */
        fun shortestArc(v1: Vec3, v2: Vec3): Quaternion =
            Quaternion(1.0f + v1 * v2, v1 % v2).normalize()

        /**
         * Creates a look-at rotation quaternion.
         * @param forward forward direction (normalized)
         * @param up up direction (normalized, not parallel to forward)
         * @return rotation quaternion
         */
        fun lookAt(forward: Vec3, up: Vec3): Quaternion {
            val right = (up % forward).normalize()
            val trace = right.x + up.y + forward.z
            return if (trace > 0.0f) {
                val s = 0.5f / sqrt(trace + 1.0f)
                Quaternion(0.25f / s, Vec3((up.z - forward.y) * s, (forward.x - right.z) * s, (right.y - up.x) * s))
            } else {
                if (right.x > up.y && right.x > forward.z) {
                    val s = 2.0f * sqrt(1.0f + right.x - up.y - forward.z)
                    Quaternion((up.z - forward.y) / s, Vec3(0.25f * s, (up.x + right.y) / s, (forward.x + right.z) / s))
                } else if (up.y > forward.z) {
                    val s = 2.0f * sqrt(1.0f + up.y - right.x - forward.z)
                    Quaternion((forward.x - right.z) / s, Vec3((up.x + right.y) / s, 0.25f * s, (forward.y + up.z) / s))
                } else {
                    val s = 2.0f * sqrt(1.0f + forward.z - right.x - up.y)
                    Quaternion((right.y - up.x) / s, Vec3((forward.x + right.z) / s, (forward.y + up.z) / s, 0.25f * s))
                }
            }
        }

        val IDENTITY = Quaternion(1f, Vec3.ZERO)

        /**
         * Spherical linear interpolation between two unit quaternions.
         * Interpolates along the shortest great-circle arc.
         * @param a start unit quaternion
         * @param b end unit quaternion
         * @param t interpolation factor [0, 1]
         * @return interpolated unit quaternion
         */
        fun slerp(a: Quaternion, b: Quaternion, t: Float): Quaternion {
            var dot = a.w * b.w + a.r * b.r
            if (dot < 0f) {
                dot = -dot
                return slerp(a, -b, t)
            }
            if (dot > 1f - 1e-4) {
                return (a * (1f - t) + b * t).normalize()
            }
            val theta = acos(dot)
            val sinTheta = sin(theta)
            val w1 = sin((1f - t) * theta) / sinTheta
            val w2 = sin(t * theta) / sinTheta
            return Quaternion(a.w * w1 + b.w * w2, a.r * w1 + b.r * w2).normalize()
        }
    }

    /** Quaternion multiplication (composition of rotations) */
    operator fun times(s: Quaternion) = Quaternion(w * s.w - r * s.r, r * s.w + s.r * w + r % s.r)

    /** Rotates a vector by this quaternion */
    operator fun times(v: Vec3) = r * (2f * (r * v)) + v * (w * w - r * r) + (r % v) * (2f * w)

    /** Negates the quaternion (represents same rotation) */
    operator fun unaryMinus() = Quaternion(-w, -r)

    /** Quaternion addition */
    operator fun plus(other: Quaternion) = Quaternion(w + other.w, r + other.r)

    /** Scalar multiplication */
    operator fun times(s: Float) = Quaternion(w * s, r * s)

    /** Returns squared length */
    fun lengthSquared() = w * w + r.lengthSquared()

    /** Returns length (magnitude) */
    fun length() = sqrt(lengthSquared())

    /** Returns rotation angle in radians (for unit quaternion) */
    fun angle() = 2f * acos(w.coerceIn(-1f, 1f))

    /** Returns normalized unit quaternion */
    fun normalize(): Quaternion {
        val invLength = 1.0f / length()
        return Quaternion(w * invLength, r * invLength)
    }

    /** Converts to 4x4 rotation matrix */
    val mat4: Mat4
        get() = Mat4(
            w * w + r.x * r.x - r.y * r.y - r.z * r.z, 2 * (r.x * r.y - w * r.z), 2 * (w * r.y + r.x * r.z), 0f,
            2 * (r.x * r.y + w * r.z), w * w - r.x * r.x + r.y * r.y - r.z * r.z, 2 * (-w * r.x + r.y * r.z), 0f,
            2 * (-w * r.y + r.x * r.z), 2 * (w * r.x + r.y * r.z), w * w - r.x * r.x - r.y * r.y + r.z * r.z, 0f,
            0f, 0f, 0f, 1f
        )

    override fun toString(): String = "($w / $r)"
}

