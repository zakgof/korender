package ltree.generator.leaf

import ltree.generator.LTree
import ltree.generator.jitter
import ltree.generator.randomOrtho
import java.util.Random

class DiagonalLeaves(val rows: Int = 11, val length: Float = 0.4f) : LeafStrategy {

    override fun generateLeaves(branch: LTree.Branch): List<LTree.Leaf> {
        val r = Random()
        val branchVector = (branch.tail - branch.head)
        val branchDir = branchVector.normalize()
        val ortho = branchDir.randomOrtho()
        val blade1 = (branchDir + ortho).normalize()
        val blade2 = (branchDir - ortho).normalize()
        val normal = (ortho % branchDir).normalize()
        return (0 until rows).map {
            val blade = if (it and 1 == 1) blade1 else blade2
            LTree.Leaf(branch.head + branchVector * (it / (rows - 1f)) * jitter(),
                blade * length * jitter(),
                normal
            )
        }
    }
}