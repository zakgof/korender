package ltree.generator

import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import kotlin.math.abs

class Plane(var center: Vec3, var normal: Vec3) {
    fun distanceTo(leaf: LTree.Leaf): Float {
        val planeDistance = (leaf.mount - center) * normal
        val projection = leaf.mount - normal * planeDistance
        return abs(planeDistance) + 0.1f * (projection - center).length() + 3.0f * (normal - leaf.normal).length()
    }
}

fun kMeans(leaves: List<LTree.Leaf>, initialPlanes: List<Plane>): Map<Plane, List<LTree.Leaf>> {

    val planes = leaves.groupBy { leaf ->
        initialPlanes.minBy { plane -> plane.distanceTo(leaf) }
    }.toMutableMap()

    for (i in 0..1024) {

        planes.forEach {
            val center = it.value.fold(0.x) { a, leaf -> a + leaf.mount } * (1f / it.value.size)
            val normal = it.value.fold(0.x) { a, leaf -> a + leaf.normal }.normalize()
            it.key.center = center
            it.key.normal = normal
        }

        val newPlanes = leaves.groupBy { leaf ->
            planes.keys.minBy { plane -> plane.distanceTo(leaf) }
        }
        planes.clear()
        planes += newPlanes

        val metric = planes.entries.sumOf {
            it.value.sumOf { l -> it.key.distanceTo(l).toDouble() }
        }.toFloat()

        println("KMeans iteration: $i   metric: $metric")
    }
    return planes
}

