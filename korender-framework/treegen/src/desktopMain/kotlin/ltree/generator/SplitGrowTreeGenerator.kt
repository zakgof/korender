package ltree.generator

import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import ltree.floatIn
import ltree.randomOrtho
import kotlin.random.Random

open class SplitGrowTreeGenerator(
    val seed: Int = 0,
    val tropism: (BranchDetail) -> Float = { 0f },
    val maxAge: Float = 9f,
    val maxBranches: Int = 4096,
    val branchingStrategy: BranchingStrategy = SplitOrGrowStrategy(),
    val leafStrategy: LeafStrategy = RowanLeavesStrategy(7, 5f),
    val thicknessRatio: Float = 1.1f
    ) : TreeGenerator {

    fun interface BranchingStrategy {
        fun step(branch: BranchDetail, r: Random): List<Pair<Vec3, Float>>
    }

    fun interface LeafStrategy {
        fun leaves(branch: BranchDetail, r: Random): List<LTree.Leaf>
    }

    class SplitOrGrowStrategy : BranchingStrategy {

        override fun step(branch: BranchDetail, r: Random): List<Pair<Vec3, Float>> =
            if (r.nextFloat() < 0.2f) split(branch, r) else listOf(grow(branch, r))

        private fun split(branch: BranchDetail, r: Random): List<Pair<Vec3, Float>> =
            (0 until 2).map {
                val vector = branch.head - branch.tail
                val ortho = vector.randomOrtho(r)
                val left = r.floatIn(0.1f, 0.9f)
                val length = r.floatIn(0.3f, 0.6f)
                val newVec = (vector.normalize() + ortho * left).normalize() * length
                newVec to branch.age + length
            }

        private fun grow(branch: BranchDetail, r: Random): Pair<Vec3, Float> {
            val vector = branch.head - branch.tail
            val ortho = vector.randomOrtho(r)
            val dir = (vector.normalize() + ortho * r.floatIn(0.05f, 0.2f)).normalize()
            val length = r.floatIn(0.3f, 0.6f)
            return dir * length to branch.age + length
        }

    }

    class RowanLeavesStrategy(val rows: Int = 7, val minAge: Float) : LeafStrategy {
        override fun leaves(branch: BranchDetail, r: Random): List<LTree.Leaf> {
            if (branch.age < minAge)
                return listOf()

            val dir = branch.vector.randomOrtho()
            val normal = (dir % branch.vector).normalize()
            return (0 until rows).flatMap {
                listOf(-1f, 1f).map { mult ->
                    LTree.Leaf(branch.head + branch.vector * ((it + 0.5f) / (rows - 1)), dir * mult, normal * mult, 0.1f)
                }
            } + LTree.Leaf(branch.tail, branch.vector.normalize(), normal, 0.1f)
        }
    }

    interface BranchDetail {
        val head: Vec3
        val tail: Vec3
        val age: Float
        val vector: Vec3
            get() = tail - head
    }

    private class LBranch(
        override val head: Vec3,
        override val tail: Vec3,
        override val age: Float = 0f,
        override var raidusAtHead: Float = 0.05f,
        override var raidusAtTail: Float = 0.05f,
        var splittable: Boolean = true,
        val children: MutableList<LBranch> = mutableListOf()
    ) : LTree.Branch, BranchDetail

    override fun generateTree(): LTree {
        val r = Random(seed)
        val branches = generateBranches(r)
        val leaves = branches.flatMap { leafStrategy.leaves(it, r) }
        return LTree(branches, leaves)
    }

    private fun generateBranches(r: Random): List<LBranch> {
        val branches = mutableListOf<LBranch>()

        fun metric(candidates: List<LBranch>) =
            candidates.sumOf { candidate ->
                branches.sumOf { b -> 1.0 / (b.tail - candidate.tail).length() }
            } + candidates.sumOf { c1 ->
                candidates.filter { c2 -> c1 !== c2 }
                    .sumOf { c2 -> 1.0 / (c2.tail - c1.tail).length() }
            } + candidates.sumOf { candidate ->
                tropism(candidate).toDouble()
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

        fun step(splitter: LBranch) = bestFor(splitter) {
            branchingStrategy.step(splitter, r)
                .map {
                    LBranch(splitter.tail, splitter.tail + it.first, it.second)
                }
        }

        fun thicknessDance(branch: LBranch) {
            branch.children.forEach { thicknessDance(it) }
            if (branch.children.isEmpty()) {
                branch.raidusAtTail = 0f
                branch.raidusAtHead = 0.01f
            } else {
                branch.raidusAtTail = branch.children.maxOf { it.raidusAtHead.toDouble() }.toFloat()
                branch.raidusAtHead = branch.raidusAtTail * thicknessRatio
            }
        }

        val root = LBranch(-4.y, -3.y)
        branches += root
        while (branches.size < maxBranches) {
            val splitters = branches.filter {
                it.splittable && it.age < maxAge
            }
            if (splitters.isEmpty())
                break
            val splitter = splitters[r.nextInt(splitters.size)]
            step(splitter)
            println("Total branches: ${branches.size}   Remaining splitters: ${splitters.size}")
        }
        thicknessDance(root)
        return branches
    }
}