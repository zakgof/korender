package ltree.generator

import com.zakgof.korender.math.y
import ltree.floatIn
import ltree.jitter
import ltree.randomOrtho
import kotlin.math.sqrt
import kotlin.random.Random

class OakTreeGenerator : SplitGrowTreeGenerator(
    maxAge = 8.0f,
    tropism = { 5.0f * sqrt(it.tail.x * it.tail.x + it.tail.z * it.tail.z) },
    thicknessRatio = 1.19f,
    branchingStrategy = { branch: BranchDetail, r: Random ->
        when {
            branch.age < 1f ->
                listOf((1.y.randomOrtho(r) * r.floatIn(0.05f, 0.16f) + 0.5f.y).normalize() * 0.5f to branch.age + 0.5f)

            branch.age in 1f..1.5f ->
                (0 until 6).map {
                    (branch.vector * 5.0f + branch.vector.randomOrtho(r)).normalize() to branch.age + 1f
                }

            branch.age in 1.5f..4f ->
                listOf(
                    (branch.vector.randomOrtho(r) * r.floatIn(0.02f, 0.04f) + branch.vector).normalize() * 0.3f to branch.age + 0.3f
                )

            else ->
                (0 until 2).map {
                    (branch.vector * 3.0f + branch.vector.randomOrtho(r)).normalize() * 0.5f to branch.age + 0.5f
                }
        }
    },
    leafStrategy = { branch, r ->
        if (branch.age < 6f) {
            listOf()
        } else {
            val branchDir = branch.vector.normalize()
            val ortho = branchDir.randomOrtho()
            val blade1 = (branchDir + ortho).normalize() * 0.2f
            val blade2 = (branchDir - ortho).normalize() * 0.2f
            val normal = (ortho % branchDir).normalize()
            (0 until 5).map {
                val blade = if ((it and 1) == 1) blade1 else blade2
                LTree.Leaf(
                    branch.head + branch.vector * (it / (5 - 1f)),
                    blade * jitter(),
                    normal,
                    0.1f
                )
            }
        }
    }
)
