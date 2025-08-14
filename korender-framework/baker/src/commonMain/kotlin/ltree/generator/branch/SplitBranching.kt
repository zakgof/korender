package ltree.generator.branch

import com.zakgof.korender.math.y
import ltree.generator.LTree
import ltree.randomOrtho
import java.util.Random
import kotlin.math.sqrt

class SplitBranching : BranchStrategy {

    override fun generateBranches(): List<LTree.Branch> {
        val branches = mutableListOf<LTree.Branch>()
        val splitBranches = mutableListOf<LTree.Branch>()
        val r = Random()

        fun split(branch: LTree.Branch): List<LTree.Branch>? {

            if (splitBranches.contains(branch) || branch.level > 10)
                return null

            val look = (branch.tail - branch.head).normalize()
            val ortho = look.randomOrtho()
            val left = r.nextFloat(0.2f, 0.8f)
            val right = r.nextFloat(0.2f, 0.8f)

            val p1 = branch.tail + (look + ortho * left).normalize()
            val p2 = branch.tail + (look - ortho * right).normalize()

            return listOf(p1, p2).map { LTree.Branch(branch.level + 1, branch.tail, it, parent = branch) }
        }

        fun thicknessDance(branch: LTree.Branch) {
            branch.children.forEach { thicknessDance(it) }
            if (branch.children.isEmpty()) {
                branch.raidusAtTail = 0f
                branch.raidusAtHead = 0.01f
            } else {
                branch.raidusAtHead = sqrt(branch.children.sumOf { it.raidusAtHead.toDouble() * it.raidusAtHead.toDouble() }.toFloat())
                branch.raidusAtTail = branch.children.maxOf { it.raidusAtHead.toDouble() }.toFloat()
            }
        }


        val root = LTree.Branch(1, -4.y, 0.y, 0.1f, 0.1f, null)
        branches += root

        var metric = totalMetric(branches)

        for (iteration in 0..400) {

            val candidate = (0 until 512).mapNotNull {
                val grower = branches[r.nextInt(branches.size)]
                val newBranches = split(grower)
                newBranches?.let { grower to it }
            }.minByOrNull {
                totalMetric(branches + it.second - it.first)
            }

            if (candidate == null) break

            candidate.first.children += candidate.second
            val candidateMetric = totalMetric(branches + candidate.second - candidate.first)
            branches += candidate.second
            splitBranches += candidate.first
            println("Iteration:$iteration   metric:$metric -> candidate:$candidateMetric")
            metric = totalMetric(branches)
        }
        thicknessDance(root)
        return branches
    }

    private fun totalMetric(bs: List<LTree.Branch>) = bs.sumOf { b1 ->
        bs.filter { b2 -> b1 !== b2 }
            .sumOf { b2 -> 1.0 / (b1.tail - b2.tail).length() }
    }.toFloat() +
            bs.sumOf { b ->
                b.tail.y * -10.0
            }.toFloat() // tropism
}