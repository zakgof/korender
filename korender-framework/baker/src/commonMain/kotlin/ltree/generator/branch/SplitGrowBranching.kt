package ltree.generator.branch

import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import ltree.floatIn
import ltree.generator.LTree
import ltree.randomOrtho
import kotlin.random.Random

class SplitGrowBranching(
    val seed: Int = 0,
    val tropism: (Float, Vec3) -> Float = { l, t -> 1f * t.y },
    val splitChance: (Float, Vec3) -> Float = { l, t -> l / 8f },
    val maxAccumulatedLength: Float = 9f,
    val maxBranches: Int = 4096,

    val splitStrategy: SplitStrategy = SimpleSplitStrategy(),
    val growStrategy: GrowStrategy = SimpleGrowStrategy()

) : BranchStrategy {

    interface SplitStrategy {
        fun split(tail: Vec3, vector: Vec3, accumulatedLength: Float, r: Random): List<Pair<Vec3, Float>>
    }

    interface GrowStrategy {
        fun grow(tail: Vec3, vector: Vec3, accumulatedLength: Float, r: Random): Pair<Vec3, Float>
    }

    class SimpleSplitStrategy : SplitStrategy {
        override fun split(tail: Vec3, vector: Vec3, accumulatedLength: Float, r: Random): List<Pair<Vec3, Float>> =
            (0 until 2).map {
                val ortho = vector.randomOrtho(r)
                val left = r.floatIn(0.1f, 0.9f)
                val length = r.floatIn(0.3f, 0.6f)
                val newVec = (vector.normalize() + ortho * left).normalize() * length
                newVec to accumulatedLength + length
            }
    }

    class SimpleGrowStrategy : GrowStrategy {
        override fun grow(tail: Vec3, vector: Vec3, accumulatedLength: Float, r: Random): Pair<Vec3, Float> {
            val ortho = vector.randomOrtho(r)
            val dir = (vector.normalize() + ortho * r.floatIn(0.05f, 0.2f)).normalize()
            val length = r.floatIn(0.3f, 0.6f)
            return dir * length to accumulatedLength + length
        }
    }

    private class LBranch(
        override val head: Vec3,
        override val tail: Vec3,
        val accumulatedLength: Float = 0f,
        override var raidusAtHead: Float = 0.05f,
        override var raidusAtTail: Float = 0.05f,
        var splittable: Boolean = true,
        val children: MutableList<LBranch> = mutableListOf()
    ) : LTree.Branch

    override fun generateBranches(): List<LTree.Branch> {
        val r = Random(seed)
        val branches = mutableListOf<LBranch>()

        fun metric(candidates: List<LBranch>) =
            candidates.sumOf { candidate ->
                branches.sumOf { b -> 1.0 / (b.tail - candidate.tail).length() }
            } + candidates.sumOf { c1 ->
                candidates.filter { c2 -> c1 !== c2 }
                    .sumOf { c2 -> 1.0 / (c2.tail - c1.tail).length() }
            } + candidates.sumOf { candidate ->
                tropism(candidate.accumulatedLength, candidate.tail).toDouble()
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

        fun split(splitter: LBranch) = bestFor(splitter) {
            splitStrategy.split(splitter.tail, splitter.tail - splitter.head, splitter.accumulatedLength, r)
                .map {
                    LBranch(splitter.tail, splitter.tail + it.first, it.second)
                }
        }

        fun grow(splitter: LBranch) = bestFor(splitter) {
            listOf(growStrategy.grow(splitter.tail, splitter.tail - splitter.head, splitter.accumulatedLength, r).let {
                LBranch(splitter.tail, splitter.tail + it.first, it.second)
            })
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
        while (branches.size < maxBranches) {
            val splitters = branches.filter {
                it.splittable && it.accumulatedLength < maxAccumulatedLength
            }
            if (splitters.isEmpty())
                break
            val splitter = splitters[r.nextInt(splitters.size)]

            if (r.nextFloat() < splitChance(splitter.accumulatedLength, splitter.tail))
                split(splitter)
            else
                grow(splitter)

            println("Total branches: ${branches.size}   Remaining splitters: ${splitters.size}")
        }
        thicknessDance(root)
        return branches
    }
}