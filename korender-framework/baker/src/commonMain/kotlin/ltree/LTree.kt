package ltree

import com.zakgof.korender.math.FloatMath.PI
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import smile.clustering.DBSCAN
import java.util.Random
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class LTree(
    val branches: List<Branch>,
    val leafs: List<Leaf>,
    val attractors: List<Vec3>
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
        val bladeDir: Vec3,
        val normal: Vec3
    )
}

fun generateLTree(lTreeDef: LTreeDef): LTree {

    val attractors = initializeAttractors(lTreeDef)
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
        leaves += branches
            .filter { it.children.isEmpty() || it.level > 7 }
            .flatMap { branch ->
                val branchVector = (branch.tail - branch.head)
                val dir = branchVector.normalize().randomOrtho()
                val normal = (dir % branchVector).normalize()

                val r =
                    (0 until 8).map {
                        val mount = branch.head + branchVector * ((it + 0.5f) / 8f)
                        LTree.Leaf(mount, dir, normal)
                    } +
                            (0 until 8).map {
                                val mount = branch.head + branchVector * ((it + 0.5f) / 8f)
                                LTree.Leaf(mount, -dir, -normal)
                            } + LTree.Leaf(branch.tail, branchVector.normalize(), normal)
                r
            }
    }

    fun clusterLeaves() {

        val dbscan = DBSCAN<LTree.Leaf>.fit(
            leaves.toTypedArray(),
            {
                l1: LTree.Leaf, l2: LTree.Leaf ->
                val d = (l1.mount - l2.mount).lengthSquared() + 7.0 * (l1.normal - l2.normal).lengthSquared()
                d
            },
            8,
            1.9
        )

        println("DBSCAN clusters: ${dbscan.k()}")

        val groups = leaves.indices.groupBy { dbscan.group()[it] }
        val clusteredLeaves = groups.values.flatMap { groupIndices ->
            val groupLeaves = groupIndices.map { leaves[it] }
            val cardNormal = groupLeaves.fold(0.y) { a, c -> a + c.normal }.normalize()
            val cardPosition = groupLeaves.fold(0.y) { a, c -> a + c.mount } * (1f / groupLeaves.size)
            val fixedLeaves = groupLeaves.map { l ->
                LTree.Leaf(
                    mount = l.mount - cardNormal * ((l.mount - cardPosition) * cardNormal),
                    bladeDir = l.bladeDir -cardNormal * (l.bladeDir * cardNormal),
                    normal = cardNormal
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
    return LTree(branches, leaves, attractors)
}

fun initializeAttractors(lTreeDef: LTreeDef) =
    grid(
        -10f to 10f,
        0f to 20f,
        -10f to 10f,
        24
    )
        .filter {
            lTreeDef.sdf(it) < 0.0f
        }.toMutableList()

private fun grid(xRange: Pair<Float, Float>, yRange: Pair<Float, Float>, zRange: Pair<Float, Float>, steps: Int): List<Vec3> {
    val r = Random()
    return (0..steps).flatMap { xx ->
        (0..steps).flatMap { yy ->
            (0..steps).map { zz ->
                Vec3(
                    xRange.first + (xx + r.nextFloat(0.4f)) * (xRange.second - xRange.first) / steps,
                    yRange.first + (yy + r.nextFloat(0.4f)) * (yRange.second - yRange.first) / steps,
                    zRange.first + (zz + r.nextFloat(0.4f)) * (zRange.second - zRange.first) / steps
                )
            }
        }
    }
}

private fun Vec3.randomOrtho(): Vec3 {
    val reference = if (abs(this.x) < 0.99) 1.x else 1.y
    val ortho1 = (this % reference).normalize()
    val ortho2 = (this % ortho1).normalize()
    val angle = Random().nextFloat(2f * PI)
    return ortho1 * cos(angle) + ortho2 * sin(angle)
}



