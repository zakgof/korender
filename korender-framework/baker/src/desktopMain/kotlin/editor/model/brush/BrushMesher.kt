package editor.model.brush

import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.uuid.ExperimentalUuidApi

class BrushMesh(
    val points: List<Vec3>,
    val edges: List<Pair<Int, Int>>,
    val faces: Map<Face, List<Tri>>,
) {
    class Tri(
        val points: List<Vec3>,
        val tex: List<Vec2>,
        val normals: List<Vec3>,
    )
}


object BrushMesher {

    const val EPS = 1e-3f

    fun collectPoints(brush: Brush): MutableList<Brush.Point> {
        val points = mutableListOf<Brush.Point>()

        for (i in brush.faces.indices)
            for (j in i + 1 until brush.faces.size)
                for (k in j + 1 until brush.faces.size) {

                    val p = intersectThreePlanes(
                        brush.faces[i].plane, brush.faces[j].plane, brush.faces[k].plane
                    ) ?: continue

                    if (brush.faces.all { it.plane.distanceTo(p) <= EPS }) {
                        if (points.none { (it.pos - p).lengthSquared() < EPS * EPS }) {
                            points += Brush.Point(p, listOf(brush.faces[i].plane, brush.faces[j].plane, brush.faces[k].plane))
                        }
                    }
                }
        return points
    }

    fun buildBrushMesh(brush: Brush, points: List<Brush.Point>): BrushMesh {

        val edges = HashSet<Pair<Int, Int>>()

        fun edge(a: Int, b: Int): Pair<Int, Int> =
            if (a < b) a to b else b to a

        val faces = mutableMapOf<Face, List<BrushMesh.Tri>>()

        for (face in brush.faces) {

            val triangles = mutableListOf<BrushMesh.Tri>()

            val plane = face.plane

            val indices = points.mapIndexedNotNull { i, p ->
                if (abs(plane.distanceTo(p.pos)) < EPS) i else null
            }

            if (indices.size < 3) continue

            val n = plane.normal
            val u = n.anyPerpendicular().normalize()
            val v = n.cross(u)

            val center = indices
                .map { points[it].pos }
                .reduce(Vec3::plus) / indices.size.toFloat()

            val sorted = indices.sortedBy {
                val d = points[it].pos - center
                atan2(d.dot(v), d.dot(u))
            }

            for (i in sorted.indices) {
                edges += edge(
                    sorted[i],
                    sorted[(i + 1) % sorted.size]
                )
            }

            val texes1 = sorted.map { plane.tex(points[it].pos, false) }
            val texes2 = if (face.texturing.fitToFace) {
                val minU = texes1.minOf { it.x }
                val minV = texes1.minOf { it.y }
                val maxU = texes1.maxOf { it.x }
                val maxV = texes1.maxOf { it.y }
                texes1.map { Vec2((it.x - minU) / (maxU - minU), (it.y - minV) / (maxV - minV)) }
            } else texes1
            val texes = texes2.map {
                Vec2(
                    it.x * face.texturing.u.scale + face.texturing.u.offset,
                    it.y * face.texturing.v.scale + face.texturing.v.offset,
                )
            }
            for (i in 1 until sorted.size - 1) {
                val pos = listOf(points[sorted[0]].pos, points[sorted[i]].pos, points[sorted[i + 1]].pos)
                val tex = listOf(texes[0], texes[i], texes[i + 1])
                val normals = listOf(normal(plane, points[sorted[0]]), normal(plane, points[sorted[i]]), normal(plane, points[sorted[i + 1]]))
                triangles += BrushMesh.Tri(pos, tex, normals)
            }
            faces[face] = triangles
        }

        return BrushMesh(
            points = points.map { it.pos },
            edges = edges.toList(),
            faces = faces
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun normal(plane: Plane, point: Brush.Point) =
        point.planes
            .filter { it.smoothId == plane.smoothId }
            .map { it.normal }
            .fold(Vec3.ZERO) { acc, r -> acc + r }
            .normalize()

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