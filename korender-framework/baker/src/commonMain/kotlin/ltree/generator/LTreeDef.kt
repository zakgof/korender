package ltree.generator

import ltree.generator.branch.BranchStrategy
import ltree.generator.leaf.LeafStrategy

class LTreeDef(
    val branchStrategy: BranchStrategy,
    val leafStrategy: LeafStrategy,
)
