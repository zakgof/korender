package com.zakgof.korender.impl.context

import com.zakgof.korender.scope.PipeMeshScope
import com.zakgof.korender.scope.PipeMeshSequenceScope
import com.zakgof.korender.math.Vec3

internal class DefaultPipeMeshScope : PipeMeshScope {

    val sequences = mutableListOf<List<PipeNode>>()
    val cycles = mutableListOf<List<PipeNode>>()

    override fun sequence(block: PipeMeshSequenceScope.() -> Unit) {
        val pipeMeshSequenceContext = DefaultPipeMeshSequenceScope()
        block.invoke(pipeMeshSequenceContext)
        sequences += pipeMeshSequenceContext.nodes
    }

    override fun cycle(block: PipeMeshSequenceScope.() -> Unit) {
        val pipeMeshSequenceContext = DefaultPipeMeshSequenceScope()
        block.invoke(pipeMeshSequenceContext)
        cycles += pipeMeshSequenceContext.nodes
    }
}

internal class DefaultPipeMeshSequenceScope : PipeMeshSequenceScope {

    val nodes = mutableListOf<PipeNode>()

    override fun node(position: Vec3, radius: Float) {
        nodes += PipeNode(position, radius)
    }
}

internal class PipeNode(val position: Vec3, val radius: Float)