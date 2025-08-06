package ltree.generator.leaf

import ltree.generator.LTree
import ltree.generator.randomOrtho

class RowanLeaves(val rows: Int = 7) : LeafStrategy {

    override fun generateLeaves(branch: LTree.Branch): List<LTree.Leaf> {
        val branchVector = (branch.tail - branch.head)
        val dir = branchVector.normalize().randomOrtho()
        val normal = (dir % branchVector).normalize()
        return (0 until rows).flatMap {
            listOf(-1f, 1f).map { mult ->
                LTree.Leaf(branch.head + branchVector * ((it + 0.5f) / 8f), dir * mult, normal * mult)
            }
        } + LTree.Leaf(branch.tail, branchVector.normalize(), normal)
    }
}