package com.zakgof.korender.math

import com.zakgof.korender.math.FloatMath.cos
import com.zakgof.korender.math.FloatMath.sin

class Transform(private val matrix: Mat4 = Mat4.IDENTITY) {

    fun mat4(): Mat4 = matrix

    fun rotate(q: Quaternion): Transform =
        Transform(q.mat4() * matrix)

    fun rotate(u: Vec3, radians: Float): Transform {
        val cos = cos(radians)
        val sin = sin(radians)
        return Transform(
            Mat4(
                cos + u.x * u.x * (1f - cos),
                u.x * u.y * (1f - cos) - u.z * sin,
                u.x * u.z * (1f - cos) + u.y * sin,
                0f,
                u.y * u.x * (1f - cos) + u.z * sin,
                cos + u.y * u.y * (1f - cos),
                u.y * u.z * (1f - cos) - u.x * sin,
                0f,
                u.z * u.x * (1f - cos) - u.y * sin,
                u.z * u.y * (1f - cos) + u.x * sin,
                cos + u.z * u.z * (1f - cos),
                0f,
                0f,
                0f,
                0f,
                1f
            ) * matrix
        )
    }

    fun translate(offset: Vec3): Transform = Transform(
        Mat4(
            1f, 0f, 0f, offset.x,
            0f, 1f, 0f, offset.y,
            0f, 0f, 1f, offset.z,
            0f, 0f, 0f, 1f
        ) * matrix
    )


    fun scale(s: Float): Transform = Transform(
        Mat4(
            s, 0f, 0f, 0f,
            0f, s, 0f, 0f,
            0f, 0f, s, 0f,
            0f, 0f, 0f, 1f
        ) * matrix
    )

    fun scale(xs: Float, ys: Float, zs: Float): Transform = Transform(
        Mat4(
            xs, 0f, 0f, 0f,
            0f, ys, 0f, 0f,
            0f, 0f, zs, 0f,
            0f, 0f, 0f, 1f
        ) * matrix
    )

}