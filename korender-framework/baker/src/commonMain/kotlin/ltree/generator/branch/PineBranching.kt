package ltree.generator.branch

import com.zakgof.korender.math.y
import ltree.floatIn
import ltree.randomOrtho
import kotlin.random.Random

class PineBranching : SplitGrowBranching(
    maxAge = 15f,
    branchingStrategy = { branch: BranchDetail, r: Random ->
        if (branch.age < 1f) {
            listOf(
                (1.y.randomOrtho(r) * r.floatIn(0.01f, 0.06f) + 0.3f.y) to branch.age + 0.3f
            )
        } else if (branch.age < 7f) {
            listOf(
                (1.y.randomOrtho(r) * r.floatIn(0.01f, 0.06f) + 0.3f.y) to branch.age + 0.3f,
                (1.y.randomOrtho(r) - branch.vector).normalize() * 1.0f to 10f + branch.age
            )
        } else if (branch.vector.y < 0f) {
            val right = (1.y % branch.vector).normalize() * r.floatIn(0.5f, 1.0f)
            listOf(
                (branch.vector + right).normalize() to branch.age + 1f,
                (branch.vector - right).normalize() to branch.age + 1f
            )
        } else {
            listOf()
        }
    }
)
