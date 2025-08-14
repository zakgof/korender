package ltree.generator.branch

import ltree.generator.LTree

interface BranchStrategy {
    fun generateBranches(): List<LTree.Branch>
}