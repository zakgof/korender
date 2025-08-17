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

        fun bestFor(splitter: LBranch, split: () -> List<LBranch>) {
            val winner = (0 until 32).map {
                splitter to split()
            }.minBy {
                metric(it.second)
            }
            branches += winner.second
            winner.first.splittable = false
            winner.first.children += winner.second
        }

        fun split(splitter: LBranch, count: Int) = bestFor(splitter) {
            val look = (splitter.tail - splitter.head).normalize()
            (0 until count).map {
                val ortho = look.randomOrtho(r)
                val left = r.floatIn(0.1f, 0.9f)
                val length = r.floatIn(0.3f, 0.6f)
                val tail = splitter.tail + (look + ortho * left).normalize() * length
                LBranch(splitter.tail, tail, splitter.accumulatedLength + length, splitter)
            }
        }

        fun grow(splitter: LBranch) = bestFor(splitter) {
            val look = (splitter.tail - splitter.head).normalize()
            val ortho = look.randomOrtho(r)
            val dir = (look + ortho * r.floatIn(0.05f, 0.2f)).normalize()
            val length = r.floatIn(0.3f, 0.6f)
            val tail = splitter.tail + dir * length
            listOf(LBranch(splitter.tail, tail, splitter.accumulatedLength + length, splitter))
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
        while (branches.size < 8192) {
            val splitters = branches.filter { it.canGrow() }
            if (splitters.isEmpty())
                break
            val splitter = splitters[r.nextInt(splitters.size)]

            val splitChance = when (splitter.accumulatedLength) {
                in 0f..1f -> 1.0f
                in 2f..7f -> 0.04f
                in 7f..13f -> 0.6f
                else -> 0f
            }

            val splitCount = when (splitter.accumulatedLength) {
                in 0f..1f -> 3
                else -> 2
            }

            if (r.nextFloat() < splitChance)
                split(splitter, splitCount)
            else
                grow(splitter)

            println("Total branches: ${branches.size}   Remaining splitters: ${splitters.size}")
        }
        thicknessDance(root)
        return branches
    }
}