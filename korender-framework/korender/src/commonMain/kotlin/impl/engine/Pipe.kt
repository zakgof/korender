package com.zakgof.korender.impl.engine

import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.SCALE
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.MeshInitializer
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.context.PipeMeshContext
import com.zakgof.korender.impl.context.DefaultPipeMeshContext
import com.zakgof.korender.impl.context.PipeNode
import com.zakgof.korender.impl.geometry.CustomMesh
import com.zakgof.korender.math.Vec3
import kotlin.math.sqrt

internal fun createPipeMesh(id: String, segments: Int, dynamic: Boolean, retentionPolicy: RetentionPolicy, block: PipeMeshContext.() -> Unit) =
    CustomMesh(id, segments * 4, segments * 6, listOf(POS, NORMAL, TEX, SCALE), dynamic, indexType = null, retentionPolicy) {
        val pipeMeshContext = DefaultPipeMeshContext()
        block.invoke(pipeMeshContext)
        pipeMeshContext.sequences.forEach { sequence ->
            sequence.dropLast(1).indices.forEach { index ->
                val node = sequence[index]
                val prevNode = if (index > 0) sequence[index - 1] else null
                val nextNode = sequence[index + 1]
                val nextNextNode = if (index + 2 < sequence.size) sequence[index + 2] else null
                pipeSegment(prevNode, node, nextNode, nextNextNode, index)
            }
        }
        pipeMeshContext.cycles.forEach { sequence ->
            sequence.indices.forEach { index ->
                val node = sequence[index]
                val prevNode = sequence[(index + sequence.size - 1) % sequence.size]
                val nextNode = sequence[(index + 1) % sequence.size]
                val nextNextNode = sequence[(index + 2) % sequence.size]
                pipeSegment(prevNode, node, nextNode, nextNextNode, index)
            }
        }
    }

private fun MeshInitializer.pipeSegment(prevNode: PipeNode?, node: PipeNode, nextNode: PipeNode, nextNextNode: PipeNode?, segmentIndex: Int) {
    val start = if (prevNode == null) node.position else jointPipes1(prevNode.position, node.position, nextNode.position, node.radius)
    val end = if (nextNextNode == null) nextNode.position else jointPipes2(node.position, nextNode.position, nextNextNode.position, nextNode.radius)
    val t = end - start
    pos(start).normal(t).tex(0f, 0f).attr(SCALE, node.radius, nextNode.radius)
    pos(start).normal(t).tex(1f, 0f).attr(SCALE, node.radius, nextNode.radius)
    pos(start).normal(t).tex(1f, 1f).attr(SCALE, node.radius, nextNode.radius)
    pos(start).normal(t).tex(0f, 1f).attr(SCALE, node.radius, nextNode.radius)
    index(segmentIndex * 4 + 0, segmentIndex * 4 + 1, segmentIndex * 4 + 2, segmentIndex * 4 + 0, segmentIndex * 4 + 2, segmentIndex * 4 + 3)
}

private fun jointPipes1(p1: Vec3, p2: Vec3, p3: Vec3, radius: Float): Vec3 {
    val u1 = (p2 - p1).normalize()
    val u2 = (p3 - p2).normalize()
    val c = u1 * u2
    return p2 - u2 * (radius * sqrt((1f - c) / (1f + c)))
}

private fun jointPipes2(p1: Vec3, p2: Vec3, p3: Vec3, radius: Float): Vec3 {
    val u1 = (p2 - p1).normalize()
    val u2 = (p3 - p2).normalize()
    val c = u1 * u2
    return p2 + u1 * (radius * sqrt((1f - c) / (1f + c)))
}
