package com.zakgof.korender.examples.gltfviewer

import com.zakgof.korender.Mesh
import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.gltf.GltfUpdate
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import kotlin.math.atan
import kotlin.math.tan

class BoundingSphere(val center: Vec3, val radius: Float) {

    companion object {
        fun fromPoints(points: List<Vec3>): BoundingSphere {
            if (points.isEmpty()) {
                return BoundingSphere(Vec3.ZERO, 0f)
            }
            val p0 = points[0]
            val p1 = points.maxBy { (it - p0) * (it - p0) }
            val p2 = points.maxBy { (it - p1) * (it - p1) }
            var center = (p1 + p2) * 0.5f
            var radius = ((p2 - center).length())
            for (p in points) {
                val diff = p - center
                val dist = diff.length()
                if (dist > radius) {
                    val newRadius = (radius + dist) * 0.5f
                    val k = (newRadius - radius) / dist
                    center += diff * k
                    radius = newRadius
                }
            }
            return BoundingSphere(center, radius)
        }

        fun merge(mergeables: List<BoundingSphere>): BoundingSphere {
            if (mergeables.isEmpty()) {
                return BoundingSphere(Vec3.ZERO, 0f)
            }
            var c = mergeables[0].center
            var r = mergeables[0].radius
            for (s in mergeables.drop(1)) {
                val diff = s.center - c
                val dist = diff.length()
                if (dist + s.radius <= r) continue
                if (dist + r <= s.radius) {
                    c = s.center
                    r = s.radius
                    continue
                }
                val newRadius = (r + dist + s.radius) * 0.5f
                val k = (newRadius - r) / dist
                c += diff * k
                r = newRadius
            }
            return BoundingSphere(c, r)
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

fun KorenderContext.cameraFor(bs: BoundingSphere) = camera(bs.center - (bs.radius * 3f).z, 1.z, 1.y)

fun KorenderContext.projectionFor(bs: BoundingSphere, whr: Float): ProjectionDeclaration {
    val near = 2f * bs.radius
    val far = 4f * bs.radius
    val base = atan(1f / 3f)
    val fovY2 = if (whr >= 1f) base else atan(tan(base) / whr)
    val halfHeight = near * tan(fovY2)
    val halfWidth = halfHeight * whr
    return projection(
        width = 2f * halfWidth,
        height = 2f * halfHeight,
        near = near,
        far = far
    )
}
