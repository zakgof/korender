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
        fun rotate(point: Vec3, axis: Vec3, angle: Float): Transform = IDENTITY.rotate(point, axis, angle)
        fun rotate(direction: Vec3, uptrend: Vec3): Transform = IDENTITY.rotate(direction, uptrend)
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

    fun rotate(q: Quaternion): Transform = Transform(q.mat4 * mat4)

    fun rotate(point: Vec3, axis: Vec3, angle: Float): Transform {
        val ux = axis.x;
        val uy = axis.y;
        val uz = axis.z
        val c = cos(angle)
        val s = sin(angle)
        val v = 1f - c

        val r00 = ux * ux * v + c
        val r01 = ux * uy * v - uz * s
        val r02 = ux * uz * v + uy * s
        val r10 = uy * ux * v + uz * s
        val r11 = uy * uy * v + c
        val r12 = uy * uz * v - ux * s
        val r20 = uz * ux * v - uy * s
        val r21 = uz * uy * v + ux * s
        val r22 = uz * uz * v + c

        val px = point.x;
        val py = point.y;
        val pz = point.z
        val tx = px - (r00 * px + r01 * py + r02 * pz)
        val ty = py - (r10 * px + r11 * py + r12 * pz)
        val tz = pz - (r20 * px + r21 * py + r22 * pz)

        return Transform(
            Mat4(
                r00, r01, r02, tx,
                r10, r11, r12, ty,
                r20, r21, r22, tz,
                0f, 0f, 0f, 1f
            )
        )
    }

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