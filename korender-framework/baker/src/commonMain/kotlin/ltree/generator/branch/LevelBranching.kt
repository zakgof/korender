package ltree.generator.branch

import ltree.generator.LTree

class LevelBranching : BranchStrategy {

    override fun generateBranches(): List<LTree.Branch> {
        val branches = mutableListOf<LTree.Branch>()
        return branches
    }
}