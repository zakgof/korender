package ltree.generator.branch

import com.zakgof.korender.math.y
import ltree.generator.LTree
import ltree.randomOrtho
import kotlin.random.Random

class LevelBranching : BranchStrategy {

    override fun generateBranches(): List<LTree.Branch> {
        val r = Random(0)
        val branches = mutableListOf<LTree.Branch>()

        fun metric(candidate: LTree.Branch) = branches.sumOf { b -> 1.0/ (b.tail - candidate.tail).length() } +
                0.01 * candidate.tail.y

        fun split(parent: LTree.Branch): List<LTree.Branch> {
            val childrenCount = 15
            return (1 until childrenCount).map { c ->
                val along = (parent.tail - parent.head)
                val mount = parent.head + along * (c / childrenCount.toFloat())
                val winner = (0 until 128).map {
                    val dir = ((parent.tail - parent.head).randomOrtho(r) + along.normalize() * (0.8f / parent.level)).normalize()
                    val length = r.nextFloat() * (childrenCount.toFloat() - c) / parent.level / parent.level
                    LTree.Branch(parent.level + 1, mount, mount + dir * length, 0.2f / parent.level, 0.0f, parent)
                }.minBy {
                    println(" Candidate metric ${metric(it)}")
                    metric(it)
                }
                println("Winner metric ${metric(winner)}")
                branches += winner
                winner
            }
        }

        fun danceWith(splitters: List<LTree.Branch>) {
            val children = splitters
                .filter { it.level < 3 }
                .flatMap { split(it) }
            if (children.isNotEmpty()) {
                danceWith(children)
            }
        }

        val root = LTree.Branch(1, -4.y, 10.y, 0.6f, 0.03f, null)
        branches += root
        danceWith(listOf(root))
        return branches
    }
}