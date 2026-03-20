package com.zakgof.korender.baker.editor.walk

import com.zakgof.korender.math.Vec3
import editor.model.BoundingBox
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlin.math.abs

@OptIn(ExperimentalSerializationApi::class)
class Collider(bvhBytes: ByteArray) {

    val bvh: WalkBvhNode = Cbor.decodeFromByteArray<WalkSerialModel.BvhNode>(bvhBytes).inflate()

    fun test(start: Vec3, delta: Vec3, extents: Vec3): Hit? {
        val sweptAABB = computeSweptAABB(start, delta, extents * 0.5f)
        val candidates = mutableListOf<WalkBrush>()
        query(bvh, sweptAABB, candidates)
        var bestHit: Hit? = null
        for (brush in candidates) {
            val expanded = brush.planes.map { expandPlane(it, extents) }
            val hit = sweepPointVsBrush(start, delta, brush.copy(planes = expanded))
            if (hit != null && (bestHit == null || hit.t < bestHit.t)) bestHit = hit
        }
        return bestHit
    }

    private fun query(node: WalkBvhNode, area: BoundingBox, out: MutableList<WalkBrush>) {
        if (!node.bb.intersects(area)) return
        node.brushes?.let { out += it }
        node.left?.let { query(it, area, out) }
        node.right?.let { query(it, area, out) }
    }

    private fun sweepPointVsBrush(start: Vec3, delta: Vec3, brush: WalkBrush): Hit? {
        var tEnter = 0f
        var tExit = 1f
        var hitNormal: Vec3? = null
        for (plane in brush.planes) {
            val d0 = plane.distance(start)
            val d1 = plane.distance(start + delta)
            if (d0 > 0f && d1 > 0f) return null
            if (d0 > 0f && d1 <= 0f) {
                val t = d0 / (d0 - d1)
                if (t > tEnter) {
                    tEnter = t
                    hitNormal = plane.normal
                }
            }
            if (d0 <= 0f && d1 > 0f) {
                val t = d0 / (d0 - d1)
                tExit = minOf(tExit, t)
            }
            if (tEnter > tExit) return null
        }
        return if (tEnter in 0f..1f && hitNormal != null) Hit(tEnter, hitNormal) else null
    }

    private fun expandPlane(plane: WalkPlane, extents: Vec3): WalkPlane {
        val r = abs(plane.normal.x) * extents.x + abs(plane.normal.y) * extents.y + abs(plane.normal.z) * extents.z
        return WalkPlane(plane.normal, plane.d - r)
    }

    fun computeSweptAABB(start: Vec3, delta: Vec3, extents: Vec3): BoundingBox {
        val startMin = start - extents
        val startMax = start + extents
        val end = start + delta
        val endMin = end - extents
        val endMax = end + extents
        val min = Vec3(
            minOf(startMin.x, endMin.x),
            minOf(startMin.y, endMin.y),
            minOf(startMin.z, endMin.z)
        )
        val max = Vec3(
            maxOf(startMax.x, endMax.x),
            maxOf(startMax.y, endMax.y),
            maxOf(startMax.z, endMax.z)
        )
        return BoundingBox(min, max)
    }
}

data class Hit(val t: Float, val normal: Vec3)

data class WalkBvhNode(
    val bb: BoundingBox,
    val left: WalkBvhNode?,
    val right: WalkBvhNode?,
    val brushes: Collection<WalkBrush>?
)

data class WalkBrush(val planes: List<WalkPlane>)

data class WalkPlane(val normal: Vec3, val d: Float) {
    fun distance(p: Vec3) = (normal dot p) + d
}
