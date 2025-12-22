package editor.model.brush

import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import kotlin.math.abs
import kotlin.math.atan2

class BrushMesh(
    val points: List<Vec3>,
    val edges: List<Pair<Int, Int>>,
    val faces: Map<Plane, List<Tri>>
) {
    class Tri(
        val points: List<Vec3>,
        val tex: List<Vec2>,
        val normal: Vec3,
    )
}

object BrushMesher {

    fun buildBrushMesh(brush: Brush): BrushMesh {
        val eps = 1e-2f

        val points = mutableListOf<Vec3>()

        fun addPoint(p: Vec3): Int {
            for (i in points.indices) {
                if ((points[i] - p).length() < eps) return i
            }
            points += p
            return points.lastIndex
        }

        // ---------- 1. vertices ----------
        for (i in brush.faces.indices)
            for (j in i + 1 until brush.faces.size)
                for (k in j + 1 until brush.faces.size) {

                    val p = intersectThreePlanes(
                        brush.faces[i].plane, brush.faces[j].plane, brush.faces[k].plane
                    ) ?: continue

                    if (brush.faces.all { it.plane.distanceTo(p) <= eps }) {
                        addPoint(p)
                    }
                }

        val edges = HashSet<Pair<Int, Int>>()


        fun edge(a: Int, b: Int): Pair<Int, Int> =
            if (a < b) a to b else b to a

        val faces = mutableMapOf<Plane, List<BrushMesh.Tri>>()

        for (face in brush.faces) {

            val triangles = mutableListOf<BrushMesh.Tri>()

            val plane = face.plane

            val indices = points.mapIndexedNotNull { i, p ->
                if (abs(plane.distanceTo(p)) < eps) i else null
            }

            if (indices.size < 3) continue

            val n = plane.normal
            val u = n.anyPerpendicular().normalize()
            val v = n.cross(u)

            val center = indices
                .map { points[it] }
                .reduce(Vec3::plus) / indices.size.toFloat()

            val sorted = indices.sortedBy {
                val d = points[it] - center
                atan2(d.dot(v), d.dot(u))
            }

            for (i in sorted.indices) {
                edges += edge(
                    sorted[i],
                    sorted[(i + 1) % sorted.size]
                )
            }

            for (i in 1 until sorted.size - 1) {
                val pos = listOf(points[sorted[0]], points[sorted[i]], points[sorted[i + 1]])
                val tex = pos.map { plane.tex(it, face.texturing.worldScale) }
                    .map {
                        Vec2(
                            it.x * face.texturing.u.scale + face.texturing.u.offset,
                            it.y * face.texturing.v.scale + face.texturing.v.offset,
                        )
                    }
                triangles += BrushMesh.Tri(pos, tex, n)
            }

            faces[plane] = triangles
        }

        return BrushMesh(
            points = points,
            edges = edges.toList(),
            faces = faces
        )
    }

    private fun intersectThreePlanes(p1: Plane, p2: Plane, p3: Plane): Vec3? {
        val eps = 1e-6f
        val n1 = p1.normal
        val n2 = p2.normal
        val n3 = p3.normal
        val denom = n1 * (n2 % n3)
        if (abs(denom) < eps) return null
        val res = (n2 % n3) * p1.d + (n3 % n1) * p2.d + (n1 % n2) * p3.d
        return res * (-1f / denom)
    }

    private fun Brush.isPointInsideAllPlanes(point: Vec3) =
        faces.all { it.plane.distanceTo(point) <= 0.001 }

    private fun Vec3.anyPerpendicular(): Vec3 =
        if (abs(x) < 0.9f) Vec3(0f, -z, y) else Vec3(-z, 0f, x)

}