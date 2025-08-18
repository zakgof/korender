package ltree.generator

import com.zakgof.korender.math.y
import ltree.floatIn
import ltree.randomOrtho
import kotlin.random.Random

class PineTreeGenerator : SplitGrowTreeGenerator(
    maxAge = 16f,
    branchingStrategy = { branch: BranchDetail, r: Random ->
        if (branch.age < 1f) {
            listOf(
                (1.y.randomOrtho(r) * r.floatIn(0.01f, 0.06f) + 0.3f.y) to branch.age + 0.3f
            )
        } else if (branch.age < 7f) {
            listOf(
                (1.y.randomOrtho(r) * r.floatIn(0.01f, 0.06f) + 0.3f.y) to branch.age + 0.3f,
                (1.y.randomOrtho(r) - branch.vector * 3.5f).normalize() * 1.0f to 10f + branch.age
            )
        } else if (branch.vector.y < 0f) {
            val right = (1.y % branch.vector).normalize() * r.floatIn(0.5f, 1.0f)
            val length = branch.age * -0.07f + 1.8f
            listOf(
                (branch.vector + right).normalize() * length to branch.age + length,
                (branch.vector - right).normalize() * length to branch.age + length
            )
        } else {
            listOf()
        }
    },
    leafStrategy = { branch, r ->
        if (branch.age < 7f)
            listOf()
        else {
            val side = (branch.vector % 1.y).normalize()
            val normal = (side % branch.vector).normalize()
            listOf(LTree.Leaf(branch.head, branch.vector, normal))
            /*
            val side = (branch.vector % 1.y).normalize()
            val normal = (side % branch.vector).normalize()
            (0 until 7).flatMap {
                listOf(-1f, 1f).map { mult ->
                    LTree.Leaf(branch.head + branch.vector * ((it + 0.5f) / 8f), side * mult, normal * mult)
                }
            } + LTree.Leaf(branch.tail, branch.vector.normalize(), normal)
             */
        }

    }
)
