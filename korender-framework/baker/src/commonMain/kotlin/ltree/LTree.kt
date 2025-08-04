package ltree

import com.zakgof.korender.math.FloatMath.PI
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import java.util.Random
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class LTree(
    val branches: List<Branch>,
    val attractors: List<Vec3>
) {

    class Branch(
        val level: Int,
        val head: Vec3,
        val tail: Vec3
    )
}

fun generateLTree(lTreeDef: LTreeDef): LTree {

    val attractors = initializeAttractors(lTreeDef)
    val branches = mutableListOf<LTree.Branch>()
    val growingBranches = mutableListOf<LTree.Branch>()
    val r = Random()

    fun totalMetric(bs: List<LTree.Branch>) = attractors.sumOf { a ->
        bs.minOf { b ->
            max((a - b.tail).lengthSquared(), 0.2f) * (3 + b.level)
        }.toDouble()
    }.toFloat()

    fun processBranch(branch: LTree.Branch): List<LTree.Branch> {

        val look = (branch.tail - branch.head).normalize()
        val ortho = look.randomOrtho()
        val p1 = branch.tail + (look + ortho * 0.7f).normalize()
        val p2 = branch.tail + (look - ortho * 0.7f).normalize()

        return listOf(p1, p2).map { LTree.Branch(branch.level + 1, branch.tail, it) }
    }


    val root = LTree.Branch(0, -4.y, 0.y)
    branches += root
    growingBranches += root

    var metric = totalMetric(branches)

    for (iteration in 0..200) {

        val candidate = (0 until 128).map {
            val grower = growingBranches[r.nextInt(growingBranches.size)]
            val newBranches = processBranch(grower)
            arrayOf(grower, newBranches[0], newBranches[1])
        }.minBy {
            totalMetric(branches + it[1] + it[2] - it[0])
        }

        val candidateMetric = totalMetric(branches + candidate[1] + candidate[2] - candidate[0])
        if (candidateMetric < metric) {
            branches += candidate[1]
            branches += candidate[2]
            growingBranches += candidate[1]
            growingBranches += candidate[2]
            growingBranches -= candidate[0]
            println("Iteration:$iteration   metric:$metric -> candidate:$candidateMetric")
            metric = totalMetric(branches)
        }
    }
    return LTree(branches, attractors)
}

fun initializeAttractors(lTreeDef: LTreeDef) =
    grid(
        -10f to 10f,
        0f to 20f,
        -10f to 10f,
        32
    )
        .filter { lTreeDef.sdf(it) < 0.0f
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



