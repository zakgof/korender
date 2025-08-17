package ltree.generator.branch

import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import ltree.floatIn
import ltree.generator.LTree
import ltree.randomOrtho
import kotlin.random.Random

class SplitGrowBranching : BranchStrategy {

    class LBranch(
        override val head: Vec3,
        override val tail: Vec3,
        val accumulatedLength: Float = 0f,
        val parent: LBranch? = null,
        override var raidusAtHead: Float = 0.05f,
        override var raidusAtTail: Float = 0.05f,
        var splittable: Boolean = true,
        val children: MutableList<LBranch> = mutableListOf<LBranch>()
    ) : LTree.Branch {
        fun canGrow() = splittable && accumulatedLength < 12f
    }

    override fun generateBranches(): List<LTree.Branch> {
        val r = Random(0)
        val branches = mutableListOf<LBranch>()

        fun metric(candidates: List<LBranch>) =
            candidates.sumOf { candidate ->
                branches.sumOf { b -> 1.0 / (b.tail - candidate.tail).length() }
            } + candidates.sumOf { c1 ->
                candidates.filter { c2 -> c1 !== c2 }
                    .sumOf { c2 -> 1.0 / (c2.tail - c1.tail).length() }
            } + candidates.sumOf { candidate ->
                -20.0 * candidate.tail.y
            }

        fun split() {
            val growers = branches.filter { it.canGrow() }
            val winner = (0 until 64).map {
                val splitter = growers[r.nextInt(growers.size)]
                val look = (splitter.tail - splitter.head).normalize()
                val ortho = look.randomOrtho(r)
                val left = r.floatIn(0.2f, 0.8f)
                val right = r.floatIn(0.2f, 0.8f)

                val length = r.floatIn(0.3f, 0.6f)

                val p1 = splitter.tail + (look + ortho * left).normalize() * length
                val p2 = splitter.tail + (look - ortho * right).normalize() * length

                splitter to listOf(p1, p2).map { LBranch(splitter.tail, it, splitter.accumulatedLength + length, splitter) }
            }.minBy {
                // println(" Candidate metric ${metric(it.second)}")
                metric(it.second)
            }
            println("Total branches: ${branches.size}   Remaining splitters: ${growers.size}")
            branches += winner.second
            winner.first.splittable = false
            winner.first.children += winner.second
        }

        fun grow() {
            val growers = branches.filter { it.canGrow() }

            val splitter = growers[r.nextInt(growers.size)]
            val look = (splitter.tail - splitter.head).normalize()
            val ortho = look.randomOrtho(r)
            val dir = look + ortho * r.floatIn(0.05f, 0.2f)

            val length = r.floatIn(0.3f, 0.6f)

            val child = LBranch(splitter.tail, splitter.tail + dir * length, splitter.accumulatedLength + length, splitter)
            branches += child
            splitter.splittable = false
            splitter.children += child
        }

        fun thicknessDance(branch: LBranch) {
            branch.children.forEach { thicknessDance(it) }
            if (branch.children.isEmpty()) {
                branch.raidusAtTail = 0f
                branch.raidusAtHead = 0.01f
            } else {
                branch.raidusAtTail = branch.children.maxOf { it.raidusAtHead.toDouble() }.toFloat()
                branch.raidusAtHead = branch.raidusAtTail * 1.1f
            }
        }

        val root = LBranch(-4.y, -2.y)
        branches += root
        while (branches.any { it.canGrow() }) {
            if (branches.size < r.nextInt(20) || branches.size > r.floatIn(200f,400f) ||
                r.nextFloat() > 0.75f)
                split()
            else
                grow()
        }
        thicknessDance(root)
        return branches
    }
}