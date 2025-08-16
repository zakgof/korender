package ltree.generator.branch

import com.zakgof.korender.math.y
import ltree.generator.LTree
import ltree.randomOrtho
import kotlin.random.Random

class LevelBranching : BranchStrategy {

    override fun generateBranches(): List<LTree.Branch> {
        val r = Random(0)
        val branches = mutableListOf<LTree.Branch>()

        fun metric(candidate: LTree.Branch) = branches.sumOf { b -> 1.0 / (b.tail - candidate.tail).length() } +
                candidate.tail.y * -10.0

        fun split(parent: LTree.Branch): List<LTree.Branch> =
            (1 until 6).map { c ->
                (0 until 128).map {
                    val mount = parent.head + (parent.tail - parent.head) * (c / 6f)
                    val dir = (parent.tail - parent.head).randomOrtho(r)
                    val length = r.nextFloat() * (6f - c)
                    LTree.Branch(parent.level + 1, mount, mount + dir * length, 0.1f, 0.1f, parent)
                }.minBy { metric(it) }
            }

        fun danceWith(splitters: List<LTree.Branch>, level: Int) {
            val children = splitters.flatMap { split(it) }
            branches += children
            if (level < 3) {
                danceWith(children, level + 1)
            }
        }

        val root = LTree.Branch(1, -4.y, 10.y, 0.3f, 0.3f, null)
        danceWith(listOf(root), 1)
        return branches
    }
}