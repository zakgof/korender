package com.zakgof.korender.math

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

class BoundingBox(val corners: Set<Vec3>) {

    constructor(points: List<Vec3>) : this(toAABB(points))

    fun transform(transform: Transform): BoundingBox = BoundingBox(corners.map { transform.mat4() * it })
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

    fun center(): Vec3 = Vec3(corners.sumOf { it.x.toDouble() }.toFloat(),
        corners.sumOf { it.y.toDouble() }.toFloat(),
        corners.sumOf { it.z.toDouble() }.toFloat()
    )

}
