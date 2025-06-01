package com.zakgof.korender.math

import com.zakgof.korender.KorenderException

class Mat4(
    val m00: Float,
    val m01: Float,
    val m02: Float,
    val m03: Float,
    val m10: Float,
    val m11: Float,
    val m12: Float,
    val m13: Float,
    val m20: Float,
    val m21: Float,
    val m22: Float,
    val m23: Float,
    val m30: Float,
    val m31: Float,
    val m32: Float,
    val m33: Float
) {
    constructor(array: FloatArray) : this(
        array[0],
        array[4],
        array[8],
        array[12],
        array[1],
        array[5],
        array[9],
        array[13],
        array[2],
        array[6],
        array[10],
        array[14],
        array[3],
        array[7],
        array[11],
        array[15]
    )

    companion object {
        val ZERO: Mat4 = Mat4(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        val IDENTITY: Mat4 = Mat4(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)
    }

    operator fun plus(a: Mat4) =
        Mat4(
            m00 + a.m00,
            m01 + a.m01,
            m02 + a.m02,
            m03 + a.m03,
            m10 + a.m10,
            m11 + a.m11,
            m12 + a.m12,
            m13 + a.m13,
            m20 + a.m20,
            m21 + a.m21,
            m22 + a.m22,
            m23 + a.m23,
            m30 + a.m30,
            m31 + a.m31,
            m32 + a.m32,
            m33 + a.m33
        )

    operator fun times(a: Float) = Mat4(
        m00 * a,
        m01 * a,
        m02 * a,
        m03 * a,
        m10 * a,
        m11 * a,
        m12 * a,
        m13 * a,
        m20 * a,
        m21 * a,
        m22 * a,
        m23 * a,
        m30 * a,
        m31 * a,
        m32 * a,
        m33 * a
    )

    operator fun times(vec: Vec3) = Vec3(
        (m00 * vec.x + m01 * vec.y + m02 * vec.z + m03),
        (m10 * vec.x + m11 * vec.y + m12 * vec.z + m13),
        (m20 * vec.x + m21 * vec.y + m22 * vec.z + m23)
    )

    fun project(vec: Vec3): Vec3 {
        val winv: Float = 1.0f / (m30 * vec.x + m31 * vec.y + m32 * vec.z + m33)
        return Vec3(
            winv * (m00 * vec.x + m01 * vec.y + m02 * vec.z + m03),
            winv * (m10 * vec.x + m11 * vec.y + m12 * vec.z + m13),
            winv * (m20 * vec.x + m21 * vec.y + m22 * vec.z + m23)
        )
    }

    fun asArray(): FloatArray = floatArrayOf(
        m00, m10, m20, m30,
        m01, m11, m21, m31,
        m02, m12, m22, m32,
        m03, m13, m23, m33
    )

    operator fun times(mat: Mat4): Mat4 = Mat4(
        m00 * mat.m00 + m01 * mat.m10 + m02 * mat.m20 + m03 * mat.m30,
        m00 * mat.m01 + m01 * mat.m11 + m02 * mat.m21 + m03 * mat.m31,
        m00 * mat.m02 + m01 * mat.m12 + m02 * mat.m22 + m03 * mat.m32,
        m00 * mat.m03 + m01 * mat.m13 + m02 * mat.m23 + m03 * mat.m33,

        m10 * mat.m00 + m11 * mat.m10 + m12 * mat.m20 + m13 * mat.m30,
        m10 * mat.m01 + m11 * mat.m11 + m12 * mat.m21 + m13 * mat.m31,
        m10 * mat.m02 + m11 * mat.m12 + m12 * mat.m22 + m13 * mat.m32,
        m10 * mat.m03 + m11 * mat.m13 + m12 * mat.m23 + m13 * mat.m33,

        m20 * mat.m00 + m21 * mat.m10 + m22 * mat.m20 + m23 * mat.m30,
        m20 * mat.m01 + m21 * mat.m11 + m22 * mat.m21 + m23 * mat.m31,
        m20 * mat.m02 + m21 * mat.m12 + m22 * mat.m22 + m23 * mat.m32,
        m20 * mat.m03 + m21 * mat.m13 + m22 * mat.m23 + m23 * mat.m33,

        m30 * mat.m00 + m31 * mat.m10 + m32 * mat.m20 + m33 * mat.m30,
        m30 * mat.m01 + m31 * mat.m11 + m32 * mat.m21 + m33 * mat.m31,
        m30 * mat.m02 + m31 * mat.m12 + m32 * mat.m22 + m33 * mat.m32,
        m30 * mat.m03 + m31 * mat.m13 + m32 * mat.m23 + m33 * mat.m33
    )

    fun invTranspose(): Mat3 {
        val determinant = m00 * (m11 * m22 - m12 * m21) -
                m01 * (m10 * m22 - m12 * m20) +
                m02 * (m10 * m21 - m11 * m20)

        if (determinant == 0.0f) {
            throw KorenderException("Singular matrix")
        }

        val invDet = 1.0f / determinant

        val adj00 = (m11 * m22 - m12 * m21) * invDet
        val adj01 = (m02 * m21 - m01 * m22) * invDet
        val adj02 = (m01 * m12 - m02 * m11) * invDet

        val adj10 = (m12 * m20 - m10 * m22) * invDet
        val adj11 = (m00 * m22 - m02 * m20) * invDet
        val adj12 = (m02 * m10 - m00 * m12) * invDet

        val adj20 = (m10 * m21 - m11 * m20) * invDet
        val adj21 = (m01 * m20 - m00 * m21) * invDet
        val adj22 = (m00 * m11 - m01 * m10) * invDet

        return Mat3(
            adj00, adj10, adj20,
            adj01, adj11, adj21,
            adj02, adj12, adj22
        )
    }
}