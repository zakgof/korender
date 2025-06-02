package com.zakgof.korender.math

import kotlin.math.cos
import kotlin.math.sin

class Transform(val mat4: Mat4 = Mat4.IDENTITY) {

    companion object {
        fun translate(offset: Vec3): Transform = IDENTITY.translate(offset)
        fun translate(x: Float, y: Float, z: Float): Transform = IDENTITY.translate(x, y, z)
        fun scale(s: Float): Transform = IDENTITY.scale(s)
        fun scale(xs: Float, ys: Float, zs: Float): Transform = IDENTITY.scale(xs, ys, zs)
        fun rotate(q: Quaternion): Transform = IDENTITY.rotate(q)
        fun rotate(axis: Vec3, angle: Float): Transform = IDENTITY.rotate(axis, angle)
        fun rotate(direction: Vec3, uptrend: Vec3): Transform =
            IDENTITY.rotate(direction, uptrend)
        val IDENTITY = Transform()
    }

    fun translate(x: Float, y: Float, z: Float) = translate(Vec3(x, y, z))

    fun translate(offset: Vec3): Transform = Transform(
        Mat4(
            1f, 0f, 0f, offset.x,
            0f, 1f, 0f, offset.y,
            0f, 0f, 1f, offset.z,
            0f, 0f, 0f, 1f
        ) * mat4
    )

    fun scale(s: Float): Transform = Transform(
        Mat4(
            s, 0f, 0f, 0f,
            0f, s, 0f, 0f,
            0f, 0f, s, 0f,
            0f, 0f, 0f, 1f
        ) * mat4
    )

    fun scale(xs: Float, ys: Float, zs: Float): Transform = Transform(
        Mat4(
            xs, 0f, 0f, 0f,
            0f, ys, 0f, 0f,
            0f, 0f, zs, 0f,
            0f, 0f, 0f, 1f
        ) * mat4
    )

    fun rotate(q: Quaternion): Transform =
        Transform(q.mat4 * mat4)

    fun rotate(axis: Vec3, angle: Float): Transform {
        val cos = cos(angle)
        val sin = sin(angle)
        return Transform(
            Mat4(
                cos + axis.x * axis.x * (1f - cos),
                axis.x * axis.y * (1f - cos) - axis.z * sin,
                axis.x * axis.z * (1f - cos) + axis.y * sin,
                0f,
                axis.y * axis.x * (1f - cos) + axis.z * sin,
                cos + axis.y * axis.y * (1f - cos),
                axis.y * axis.z * (1f - cos) - axis.x * sin,
                0f,
                axis.z * axis.x * (1f - cos) - axis.y * sin,
                axis.z * axis.y * (1f - cos) + axis.x * sin,
                cos + axis.z * axis.z * (1f - cos),
                0f,
                0f,
                0f,
                0f,
                1f
            ) * mat4
        )
    }

    fun rotate(direction: Vec3, uptrend: Vec3): Transform {
        val right = (direction % uptrend).normalize()
        val up = (right % direction).normalize()
        return Transform(
            Mat4(
                right.x, up.x, -direction.x, 0f,
                right.y, up.y, -direction.y, 0f,
                right.z, up.z, -direction.z, 0f,
                0f, 0f, 0f, 1f
            ) * mat4
        )
    }

    operator fun times(v: Vec3) = mat4 * v

    operator fun times(that: Transform) = Transform(mat4 * that.mat4)

    fun project(v: Vec3) = mat4.project(v)

    fun offset() = mat4 * Vec3.ZERO

    fun applyToDirection(v: Vec3) = mat4 * v - offset()

}