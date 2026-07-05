package com.zakgof.korender.math

/**
 * Computes axis-aligned bounding box corners from a list of points.
 * @param points list of 3D points
 * @return set of 8 AABB corners
 */
fun toAABB(points: List<Vec3>): Set<Vec3> {
    val xmin = points.minOf { it.x }
    val ymin = points.minOf { it.y }
    val zmin = points.minOf { it.z }
    val xmax = points.maxOf { it.x }
    val ymax = points.maxOf { it.y }
    val zmax = points.maxOf { it.z }
    return setOf(
        Vec3(xmin, ymin, zmin),
        Vec3(xmax, ymin, zmin),
        Vec3(xmin, ymax, zmin),
        Vec3(xmax, ymax, zmin),
        Vec3(xmin, ymin, zmax),
        Vec3(xmax, ymin, zmax),
        Vec3(xmin, ymax, zmax),
        Vec3(xmax, ymax, zmax)
    )
}

/**
 * Axis-aligned bounding box represented by its 8 corner points.
 * Used for frustum culling and spatial queries.
 * @param corners set of 8 corner points
 */
class BoundingBox(val corners: Set<Vec3>) {

    /**
     * Creates a bounding box from a list of points.
     * The box is computed as the minimum AABB enclosing all points.
     * @param points list of points to enclose
     */
    constructor(points: List<Vec3>) : this(toAABB(points))

    /**
     * Transforms the bounding box by a transform matrix.
     * @param transform transformation to apply
     * @return new transformed bounding box
     */
    fun transform(transform: Transform): BoundingBox =
        BoundingBox(corners.map { transform.mat4 * it })

    /**
     * Checks if the bounding box is inside or intersecting the view frustum.
     * @param mat4 view-projection matrix
     * @return true if the box is visible (inside or intersecting the frustum)
     */
    fun isIn(mat4: Mat4): Boolean {
        val screenSpaceCorners = corners.map { mat4.project(it) }
        return !(screenSpaceCorners.all { it.x < -1f }
                || screenSpaceCorners.all { it.x > 1f }
                || screenSpaceCorners.all { it.y < -1f }
                || screenSpaceCorners.all { it.y > 1f }
                || screenSpaceCorners.all { it.z < 0f }
                || screenSpaceCorners.all { it.z > 1f }
                )
    }

    /**
     * Computes the center point of the bounding box.
     * @return center of the box
     */
    fun center(): Vec3 = Vec3(
        corners.sumOf { it.x.toDouble() }.toFloat(),
        corners.sumOf { it.y.toDouble() }.toFloat(),
        corners.sumOf { it.z.toDouble() }.toFloat()
    ).let { it * (1f / corners.size.toFloat()) }
}