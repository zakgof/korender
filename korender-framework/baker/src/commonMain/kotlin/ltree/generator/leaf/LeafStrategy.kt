package ltree.generator.leaf

import ltree.generator.LTree

interface LeafStrategy {
    fun generateLeaves(branch: LTree.Branch): List<LTree.Leaf>
}
