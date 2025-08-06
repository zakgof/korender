package ltree.generator

import com.zakgof.korender.math.FloatMath.PI
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class LTree(
    val branches: List<Branch>,
    val leafs: List<Leaf>,
) {

    class Branch(
        val level: Int,
        val head: Vec3,
        val tail: Vec3,
        var raidusAtHead: Float = 0f,
        var raidusAtTail: Float = 0f,
        val parent: Branch?,
        val children: MutableList<Branch> = mutableListOf()
    )

    class Leaf(
        val mount: Vec3,
        val blade: Vec3,
        val normal: Vec3
    )

}

fun generateLTree(lTreeDef: LTreeDef): LTree {

    val branches = mutableListOf<LTree.Branch>()
    val splitBranches = mutableSetOf<LTree.Branch>()
    val leaves = mutableListOf<LTree.Leaf>()
    val r = Random()

    fun totalMetric(bs: List<LTree.Branch>) = bs.sumOf { b1 ->
        bs.filter { b2 -> b1 !== b2 }
            .sumOf { b2 -> 1.0 / (b1.tail - b2.tail).length() }
    }.toFloat() +
            bs.sumOf { b ->
                b.tail.y * -10.0
            }.toFloat() // tropism

    fun split(branch: LTree.Branch): List<LTree.Branch>? {

        if (splitBranches.contains(branch) || branch.level > 10)
            return null

        val look = (branch.tail - branch.head).normalize()
        val ortho = look.randomOrtho()
        val left = r.nextFloat(0.2f, 0.8f)
        val right = r.nextFloat(0.2f, 0.8f)

        val p1 = branch.tail + (look + ortho * left).normalize()
        val p2 = branch.tail + (look - ortho * right).normalize()

        return listOf(p1, p2).map { LTree.Branch(branch.level + 1, branch.tail, it, parent = branch) }
    }

    fun thicknessDance(branch: LTree.Branch) {
        branch.children.forEach { thicknessDance(it) }
        if (branch.children.isEmpty()) {
            branch.raidusAtTail = 0f
            branch.raidusAtHead = 0.01f
        } else {
            branch.raidusAtTail = sqrt(branch.children.sumOf { it.raidusAtHead.toDouble() * it.raidusAtHead.toDouble() }.toFloat())
            branch.raidusAtHead = branch.raidusAtTail
        }
    }

    fun seedLeaves() {
        leaves +=
            branches
                .filter { it.children.isEmpty() || it.level > 7 }
                .flatMap { branch -> lTreeDef.leafStrategy.generateLeaves(branch) }
    }

    fun clusterLeaves() {

        val clusters = 16

        val initialPlanes = (0 until clusters).map {
            val s = sin(it * 2.0f * PI / clusters)
            val c = cos(it * 2.0f * PI / clusters)
            val normal = Vec3(s, 0f, c)
            Plane(normal * 2.0f, normal)
        }

        val kMeans = kMeans(leaves, initialPlanes)

        val clusteredLeaves = kMeans.flatMap {
            val fixedLeaves = it.value.map { l ->
                LTree.Leaf(
                    mount = l.mount - it.key.normal * ((l.mount - it.key.center) * it.key.normal),
                    blade = l.blade - it.key.normal * (l.blade * it.key.normal),
                    normal = it.key.normal
                )
            }
            fixedLeaves
        }
        leaves.clear()
        leaves += clusteredLeaves
    }


    val root = LTree.Branch(1, -4.y, 0.y, 0.1f, 0.1f, null)
    branches += root

    var metric = totalMetric(branches)

    for (iteration in 0..400) {

        val candidate = (0 until 512).mapNotNull {
            val grower = branches[r.nextInt(branches.size)]
            val newBranches = split(grower)
            newBranches?.let { grower to it }
        }.minByOrNull {
            totalMetric(branches + it.second - it.first)
        }

        if (candidate == null) break

        candidate.first.children += candidate.second
        val candidateMetric = totalMetric(branches + candidate.second - candidate.first)
        branches += candidate.second
        splitBranches += candidate.first
        println("Iteration:$iteration   metric:$metric -> candidate:$candidateMetric")
        metric = totalMetric(branches)
    }
    thicknessDance(root)
    seedLeaves()
    clusterLeaves()
    return LTree(branches, leaves)
}



