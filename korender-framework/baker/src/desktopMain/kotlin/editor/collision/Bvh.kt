package com.zakgof.korender.baker.editor.collision

import editor.model.BoundingBox
import editor.model.brush.Brush

object BvhCompiler {

    fun compile(
        brushes: Collection<Brush>,
        maxLeafSize: Int = 4,
        depth: Int = 0,
        maxDepth: Int = 32
    ): BvhNode {
        val bb = aabbOf(brushes)

        if (brushes.size <= maxLeafSize || depth >= maxDepth) {
            return BvhNode(bb, null, null, brushes)
        }

        val axis = bb.longestAxis()
        val split = bb.center dot axis

        val leftList = mutableListOf<Brush>()
        val rightList = mutableListOf<Brush>()
        val spanningList = mutableListOf<Brush>()
        for (b in brushes) {
            when {
                b.bb.min dot axis < split -> leftList += b
                b.bb.max dot axis > split -> rightList += b
                else -> spanningList += b
            }
        }

        val total = brushes.size
        val spanningRatio = spanningList.size.toFloat() / total

        if (leftList.isEmpty() || rightList.isEmpty() || spanningRatio > 0.5f) {
            return BvhNode(bb, null, null, brushes)
        }

        val left = compile(leftList, maxLeafSize, depth + 1, maxDepth)
        val right = compile(rightList, maxLeafSize, depth + 1, maxDepth)

        return BvhNode(bb, left, right, spanningList)
    }

    private fun aabbOf(brushes: Collection<Brush>) =
        brushes.map { it.bb }.reduce(BoundingBox::merge)
}

data class BvhNode(
    val bb: BoundingBox,
    val left: BvhNode?,
    val right: BvhNode?,
    val brushes: Collection<Brush>?
)