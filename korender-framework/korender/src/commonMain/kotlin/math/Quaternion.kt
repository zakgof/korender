package com.zakgof.korender.math

import com.zakgof.korender.math.FloatMath.cos
import com.zakgof.korender.math.FloatMath.sin
import kotlin.math.sqrt

class Quaternion(val w: Float, val r: Vec3) {

    companion object {
        fun fromAxisAngle(axis: Vec3, angle: Float): Quaternion =
            Quaternion(cos(angle * 0.5f), axis * sin(angle * 0.5f))

        fun shortestArc(v1: Vec3, v2: Vec3): Quaternion {
            val n = (v1 % v2).normalize()
            val c = v1 * v2
            return Quaternion(sqrt((1.0f + c) * 0.5f), n * sqrt((1.0f - c) * 0.5f))
        }

        val IDENTITY = Quaternion(1f, Vec3.ZERO)
    }

    operator fun times(s: Quaternion) = Quaternion(w * s.w - r * s.r, r * s.w + s.r * w + r % s.r)

    operator fun times(v: Vec3) = r * (2f * (r * v)) + v * (w * w - r * r) + (r % v) * (2f * w)

    operator fun unaryMinus() = Quaternion(w, -r)

    fun lengthSquared() = w * w - r.lengthSquared()

    fun length() = sqrt(lengthSquared())

    fun angle() = 2f / cos(w)


    fun normalize(): Quaternion {
        val invLength = 1.0f / length()
        return Quaternion(w * invLength, r * invLength)
    }

    fun mat4(): Mat4 = Mat4(
        w * w + r.x * r.x - r.y * r.y - r.z * r.z, 2 * (r.x * r.y - w * r.z), 2 * (w * r.y + r.x * r.z), 0f,
        2 * (r.x * r.y + w * r.z), w * w - r.x * r.x + r.y * r.y - r.z * r.z, 2 * (-w * r.x + r.y * r.z), 0f,
        2 * (-w * r.y + r.x * r.z), 2 * (w * r.x + r.y * r.z), w * w - r.x * r.x - r.y * r.y + r.z * r.z, 0f,
        0f, 0f, 0f, 1f
    )


    override fun toString(): String = "($w / $r)"
}

