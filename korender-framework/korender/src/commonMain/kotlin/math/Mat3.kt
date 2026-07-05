package com.zakgof.korender.math

/**
 * 3x3 matrix for 3D linear transformations (rotation, scaling).
 * Used for normal matrix calculations and 2D transformations.
 *
 * @param m00 row 0, col 0
 * @param m01 row 0, col 1
 * @param m02 row 0, col 2
 * @param m10 row 1, col 0
 * @param m11 row 1, col 1
 * @param m12 row 1, col 2
 * @param m20 row 2, col 0
 * @param m21 row 2, col 1
 * @param m22 row 2, col 2
 */
class Mat3(
    val m00: Float,
    val m01: Float,
    val m02: Float,
    val m10: Float,
    val m11: Float,
    val m12: Float,
    val m20: Float,
    val m21: Float,
    val m22: Float
) {
    companion object {
        /** Zero matrix */
        val ZERO: Mat3 = Mat3(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        /** Identity matrix */
        val IDENTITY: Mat3 = Mat3(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f)
    }

    /** Scalar multiplication */
    operator fun times(a: Float): Mat3 {
        return Mat3(
            m00 * a,
            m01 * a,
            m02 * a,
            m10 * a,
            m11 * a,
            m12 * a,
            m20 * a,
            m21 * a,
            m22 * a
        )
    }

    /** Matrix-vector multiplication */
    operator fun times(vec: Vec3): Vec3 {
        return Vec3(
            (m00 * vec.x + m01 * vec.y + m02 * vec.z),
            (m10 * vec.x + m11 * vec.y + m12 * vec.z),
            (m20 * vec.x + m21 * vec.y + m22 * vec.z)
        )
    }

    /** Converts to column-major float array (OpenGL format) */
    fun asArray(): FloatArray = floatArrayOf(
        m00, m10, m20,
        m01, m11, m21,
        m02, m12, m22
    )

    /** Matrix-matrix multiplication */
    operator fun times(mat: Mat3): Mat3 = Mat3(
        m00 * mat.m00 + m01 * mat.m10 + m02 * mat.m20,
        m00 * mat.m01 + m01 * mat.m11 + m02 * mat.m21,
        m00 * mat.m02 + m01 * mat.m12 + m02 * mat.m22,

        m10 * mat.m00 + m11 * mat.m10 + m12 * mat.m20,
        m10 * mat.m01 + m11 * mat.m11 + m12 * mat.m21,
        m10 * mat.m02 + m11 * mat.m12 + m12 * mat.m22,

        m20 * mat.m00 + m21 * mat.m10 + m22 * mat.m20,
        m20 * mat.m01 + m21 * mat.m11 + m22 * mat.m21,
        m20 * mat.m02 + m21 * mat.m12 + m22 * mat.m22
    )
}