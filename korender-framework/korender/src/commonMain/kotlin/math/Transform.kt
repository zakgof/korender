package com.zakgof.korender.math

import kotlin.math.cos
import kotlin.math.sin

/**
 * 4x4 transformation matrix for 3D spatial operations.
 * Represents translation, rotation, and scaling combined.
 * Uses row-major order internally (OpenGL compatible).
 *
 * Example:
 * ```kotlin
 * val transform = Transform.IDENTITY
 *     .translate(1f, 2f, 3f)
 *     .rotate(Quaternion.fromAxisAngle(Vec3.Y, 90f.degrees))
 *     .scale(2f)
 * val worldPos = transform * localPos
 * ```
 *
 * @param mat4 underlying 4x4 matrix (default: identity)
 */
class Transform(val mat4: Mat4 = Mat4.IDENTITY) {

    companion object {
        /** Creates a translation transform. */
        fun translate(offset: Vec3): Transform = IDENTITY.translate(offset)
        /** Creates a translation transform. */
        fun translate(x: Float, y: Float, z: Float): Transform = IDENTITY.translate(x, y, z)
        /** Creates a uniform scale transform. */
        fun scale(s: Float): Transform = IDENTITY.scale(s)
        /** Creates a non-uniform scale transform. */
        fun scale(s: Vec3): Transform = IDENTITY.scale(s)
        /** Creates a non-uniform scale transform. */
        fun scale(xs: Float, ys: Float, zs: Float): Transform = IDENTITY.scale(xs, ys, zs)
        /** Creates a rotation transform from a quaternion. */
        fun rotate(q: Quaternion): Transform = IDENTITY.rotate(q)
        /** Creates a rotation transform around an axis by angle (radians). */
        fun rotate(axis: Vec3, angle: Float): Transform = IDENTITY.rotate(axis, angle)
        /** Creates a rotation transform around an axis through a point. */
        fun rotate(point: Vec3, axis: Vec3, angle: Float): Transform = IDENTITY.rotate(point, axis, angle)
        /** Creates a look-at rotation transform (forward direction with up tendency). */
        fun rotate(direction: Vec3, uptrend: Vec3): Transform = IDENTITY.rotate(direction, uptrend)
        /** Identity transform (no transformation). */
        val IDENTITY = Transform()
    }

    /**
     * Applies translation.
     * @param x X offset
     * @param y Y offset
     * @param z Z offset
     * @return new transform with translation applied
     */
    fun translate(x: Float, y: Float, z: Float) = translate(Vec3(x, y, z))

    /**
     * Applies translation.
     * @param offset translation vector
     * @return new transform with translation applied
     */
    fun translate(offset: Vec3): Transform = Transform(
        Mat4(
            1f, 0f, 0f, offset.x,
            0f, 1f, 0f, offset.y,
            0f, 0f, 1f, offset.z,
            0f, 0f, 0f, 1f
        ) * mat4
    )

    /**
     * Applies uniform scaling.
     * @param s scale factor for all axes
     * @return new transform with scaling applied
     */
    fun scale(s: Float): Transform = Transform(
        Mat4(
            s, 0f, 0f, 0f,
            0f, s, 0f, 0f,
            0f, 0f, s, 0f,
            0f, 0f, 0f, 1f
        ) * mat4
    )

    /**
     * Applies non-uniform scaling.
     * @param s scale vector (x, y, z)
     * @return new transform with scaling applied
     */
    fun scale(s: Vec3) = scale(s.x, s.y, s.z)

    /**
     * Applies non-uniform scaling.
     * @param xs X scale factor
     * @param ys Y scale factor
     * @param zs Z scale factor
     * @return new transform with scaling applied
     */
    fun scale(xs: Float, ys: Float, zs: Float): Transform = Transform(
        Mat4(
            xs, 0f, 0f, 0f,
            0f, ys, 0f, 0f,
            0f, 0f, zs, 0f,
            0f, 0f, 0f, 1f
        ) * mat4
    )

    /**
     * Applies rotation from quaternion.
     * @param q rotation quaternion
     * @return new transform with rotation applied
     */
    fun rotate(q: Quaternion): Transform = Transform(q.mat4 * mat4)

    /**
     * Applies rotation around an axis through a point.
     * @param point pivot point
     * @param axis rotation axis (normalized)
     * @param angle rotation angle in radians
     * @return new transform with rotation applied
     */
    fun rotate(point: Vec3, axis: Vec3, angle: Float): Transform {
        val ux = axis.x
        val uy = axis.y
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

        val px = point.x
        val py = point.y
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
            ) * mat4
        )
    }

    /**
     * Applies rotation around an axis through origin.
     * @param axis rotation axis (normalized)
     * @param angle rotation angle in radians
     * @return new transform with rotation applied
     */
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

    /**
     * Creates look-at rotation (forward direction with up tendency).
     * @param direction forward direction (normalized)
     * @param uptrend up tendency vector (not parallel to direction)
     * @return new transform with rotation applied
     */
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

    /**
     * Transforms a position vector (with translation).
     * @param v vector to transform
     * @return transformed vector
     */
    operator fun times(v: Vec3) = mat4 * v

    /**
     * Composes two transforms (this * that).
     * @param that transform to apply after this
     * @return composed transform
     */
    operator fun times(that: Transform) = Transform(mat4 * that.mat4)

    /**
     * Projects a vector using perspective division (for camera projection).
     * @param v vector to project
     * @return projected vector in normalized device coordinates
     */
    fun project(v: Vec3) = mat4.project(v)

    /**
     * Gets the translation component of this transform.
     * @return world position of origin after this transform
     */
    fun offset() = mat4 * Vec3.ZERO

    /**
     * Transforms a direction vector (without translation).
     * @param v direction vector
     * @return transformed direction
     */
    fun applyToDirection(v: Vec3) = mat4 * v - offset()
}