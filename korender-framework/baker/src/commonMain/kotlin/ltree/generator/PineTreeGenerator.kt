package ltree.generator

import com.zakgof.korender.math.y
import ltree.floatIn
import ltree.randomOrtho
import kotlin.random.Random

class PineTreeGenerator : SplitGrowTreeGenerator(
    maxAge = 16.0f,
    branchingStrategy = { branch: BranchDetail, r: Random ->
        if (branch.age < 1.6f) {
            listOf(
                (1.y.randomOrtho(r) * r.floatIn(0.01f, 0.06f) + 0.3f.y) to branch.age + 0.3f
            )
        } else if (branch.age < 7f) {
            listOf(
                (1.y.randomOrtho(r) * r.floatIn(0.01f, 0.06f) + 0.3f.y) to branch.age + 0.3f,
                (1.y.randomOrtho(r) - branch.vector * 2.0f).normalize() * 1.0f to 10f + branch.age
            )
        } else if (branch.vector.y < 0f) {
            val right = (1.y % branch.vector).normalize() * r.floatIn(0.5f, 1.0f)
            val length = 1.0f// branch.age * -0.09f + 1.9f
            listOf(
                (branch.vector + right).normalize() * length to branch.age + length,
                (branch.vector - right).normalize() * length to branch.age + length,
                (branch.vector).normalize() * length * 1.3f to branch.age + length * 1.3f
            )
        } else {
            listOf()
        }
    },
    leafStrategy = { branch, r ->
        if (branch.age < 7f)
            listOf()
        else {
            (0 until 3).map {
                LTree.Leaf(branch.head, branch.vector, branch.vector.randomOrtho(r))
            }
        }

    }
)
