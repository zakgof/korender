package ltree.generator

import com.zakgof.korender.math.Vec3

class LTree(
    val branches: List<Branch>,
    val leaves: List<Leaf>,
) {

    class Branch(
        val level: Int,
        val head: Vec3,
        val tail: Vec3,
        var raidusAtHead: Float = 0f,
        var raidusAtTail: Float = 0f,
        val parent: Branch?,
        val children: MutableList<Branch> = mutableListOf()
    )

    class Leaf(
        val mount: Vec3,
        val blade: Vec3,
        val normal: Vec3
    )

}

fun generateLTree(lTreeDef: LTreeDef): LTree {
    val branches = lTreeDef.branchStrategy.generateBranches()
    val leaves = branches.flatMap { lTreeDef.leafStrategy.generateLeaves(it) }
    return LTree(branches, leaves)
}



