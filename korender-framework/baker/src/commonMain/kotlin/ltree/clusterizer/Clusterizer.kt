package ltree.clusterizer

import com.zakgof.korender.math.FloatMath.PI
import com.zakgof.korender.math.Vec3
import ltree.generator.LTree
import kotlin.math.cos
import kotlin.math.sin

class ClusteredTree(
    val clusters: List<Cluster>
) {
    class Cluster(
        val plane: Plane,
        val lTree: LTree
    )
}

fun clusterizeTree(lTree: LTree): ClusteredTree {

    val clustersCount = 16

    val initialPlanes = (0 until clustersCount).map {
        val s = sin(it * 2.0f * PI / clustersCount)
        val c = cos(it * 2.0f * PI / clustersCount)
        val normal = Vec3(s, 0f, c)
        Plane(normal * 2.0f, normal)
    }

    val kMeans = kMeans(lTree.leaves, initialPlanes)

    val clusters = kMeans.map {
        ClusteredTree.Cluster(it.key, LTree(listOf(), it.value))
    }
    return ClusteredTree(clusters)

}