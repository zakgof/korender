package com.zakgof.korender.context

import com.zakgof.korender.math.Vec3

interface PipeMeshContext {
    fun sequence(block: PipeMeshSequenceContext.() -> Unit)
    fun cycle(block: PipeMeshSequenceContext.() -> Unit)
}

interface PipeMeshSequenceContext {
    fun node(position: Vec3, radius: Float)
}