package com.zakgof.korender.math

import com.zakgof.korender.math.FloatMath.cos
import com.zakgof.korender.math.FloatMath.sin
import kotlin.math.sqrt

class Quaternion(val w: Float, val r: Vec3) {

    companion object {
        fun fromAxisAngle(axis: Vec3, angle: Float): Quaternion =
            Quaternion(cos(angle * 0.5f), axis * sin(angle * 0.5f))

        fun shortestArc(v1: Vec3, v2: Vec3): Quaternion =
            Quaternion(1.0f + v1 * v2, v1 % v2).normalize()

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
    }

    operator fun times(s: Quaternion) = Quaternion(w * s.w - r * s.r, r * s.w + s.r * w + r % s.r)

    operator fun times(v: Vec3) = r * (2f * (r * v)) + v * (w * w - r * r) + (r % v) * (2f * w)

//    operator fun times(v: Vec3) : Vec3 {
//        val t = r % v
//        val u = r % t
//        return v + t * (2f * w) + u * 2f
//    }

    operator fun unaryMinus() = Quaternion(w, -r)

    fun lengthSquared() = w * w + r.lengthSquared()

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

