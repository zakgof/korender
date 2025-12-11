package com.zakgof.korender.examples.gltfviewer

import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.Mesh
import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.gltf.GltfUpdate
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Vec3
import kotlin.math.atan
import kotlin.math.tan

class BoundingSphere(val center: Vec3, val radius: Float) {

    fun contains(p: Vec3) = (center - p).lengthSquared() <= radius + 1e-6f

    companion object {
        fun fromPoints(points: List<Vec3>): BoundingSphere {
            if (points.isEmpty()) return BoundingSphere(Vec3.ZERO, 0f)
            fun far(from: Vec3) = points.maxBy { (it - from).lengthSquared() }
            val a = far(points[0])
            val b = far(a)
            var c = (a + b) * 0.5f
            var r = c distanceTo a
            for (p in points) {
                val d = c distanceTo p
                if (d > r) {
                    val nr = (r + d) * 0.5f
                    c += (p - c) * ((nr - r) / d)
                    r = nr
                }
            }
            return BoundingSphere(c, r)
        }

        fun merge(spheres: List<BoundingSphere>): BoundingSphere {
            if (spheres.isEmpty()) return BoundingSphere(Vec3.ZERO, 0f)

            fun far(s: BoundingSphere) =
                spheres.maxBy { (it.center - s.center).lengthSquared() + (it.radius - s.radius) * (it.radius - s.radius) }

            val a = far(spheres[0])
            val b = far(a)

            var center = a.center + (b.center - a.center) * 0.5f
            var radius = (a.center.distanceTo(b.center) + a.radius + b.radius) * 0.5f

            for (s in spheres) {
                val d = center.distanceTo(s.center)
                if (d + s.radius > radius) {
                    val newR = (radius + d + s.radius) * 0.5f
                    val k = (newR - radius) / d
                    center += (s.center - center) * k
                    radius = newR
                }
            }
            return BoundingSphere(center, radius)
        }
    }


    fun transform(m: Mat4) = BoundingSphere(
        Vec3(
            m.m00 * center.x + m.m01 * center.y + m.m02 * center.z + m.m03,
            m.m10 * center.x + m.m11 * center.y + m.m12 * center.z + m.m13,
            m.m20 * center.x + m.m21 * center.y + m.m22 * center.z + m.m23
        ),
        radius * maxOf(
            kotlin.math.sqrt(m.m00 * m.m00 + m.m10 * m.m10 + m.m20 * m.m20),
            kotlin.math.sqrt(m.m01 * m.m01 + m.m21 * m.m21 + m.m31 * m.m31),
            kotlin.math.sqrt(m.m02 * m.m02 + m.m12 * m.m12 + m.m22 * m.m22)
        )
    )
}

fun boundingSphere(node: GltfUpdate.Node): BoundingSphere {
    val meshSpheres = node.mesh?.let { mesh ->
        mesh.primitives.map { boundingSphere(it).transform(node.transform.mat4) }
    } ?: listOf()
    val childrenSpheres = node.children.map { boundingSphere(it) }
    return BoundingSphere.merge(meshSpheres + childrenSpheres)
}

fun boundingSphere(mesh: Mesh) = BoundingSphere.fromPoints(mesh.vertices.map { it.pos!! })

fun KorenderContext.cameraFor(bs: BoundingSphere): CameraDeclaration {
    val look = Vec3(0f, -1f, -2f).normalize()
    val up = Vec3(0f, 2f, -1f).normalize()
    return camera(bs.center - look * (3f * bs.radius), look, up)
}

fun KorenderContext.projectionFor(bs: BoundingSphere, whr: Float): ProjectionDeclaration {
    val near = 2f * bs.radius
    val far = 4f * bs.radius
    val base = atan(1f / 3f)
    val fovY2 = if (whr >= 1f) base else atan(tan(base) / whr)
    val halfHeight = near * tan(fovY2)
    val halfWidth = halfHeight * whr
    return projection(
        width = 2.2f * halfWidth,
        height = 2.2f * halfHeight,
        near = near,
        far = far
    )
}
