package com.zakgof.korender.impl.context

import com.zakgof.korender.context.PipeMeshContext
import com.zakgof.korender.context.PipeMeshSequenceContext
import com.zakgof.korender.math.Vec3

internal class DefaultPipeMeshContext : PipeMeshContext {

    val sequences = mutableListOf<List<PipeNode>>()
    val cycles = mutableListOf<List<PipeNode>>()

    override fun sequence(block: PipeMeshSequenceContext.() -> Unit) {
        val pipeMeshSequenceContext = DefaultPipeMeshSequenceContext()
        block.invoke(pipeMeshSequenceContext)
        sequences += pipeMeshSequenceContext.nodes
    }

    override fun cycle(block: PipeMeshSequenceContext.() -> Unit) {
        val pipeMeshSequenceContext = DefaultPipeMeshSequenceContext()
        block.invoke(pipeMeshSequenceContext)
        cycles += pipeMeshSequenceContext.nodes
    }
}

internal class DefaultPipeMeshSequenceContext : PipeMeshSequenceContext {

    val nodes = mutableListOf<PipeNode>()

    override fun node(position: Vec3, radius: Float) {
        nodes += PipeNode(position, radius)
    }
}

internal class PipeNode(val position: Vec3, val radius: Float)