package com.zakgof.korender.context

import com.zakgof.korender.math.Vec3

interface PipeMeshScope {
    fun sequence(block: PipeMeshSequenceScope.() -> Unit)
    fun cycle(block: PipeMeshSequenceScope.() -> Unit)
}

interface PipeMeshSequenceScope {
    fun node(position: Vec3, radius: Float)
}