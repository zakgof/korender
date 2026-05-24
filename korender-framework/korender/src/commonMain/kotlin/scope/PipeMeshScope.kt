package com.zakgof.korender.scope

import com.zakgof.korender.math.Vec3

/**
 * Scope for constructing a pipe-like 3D mesh consisting of connected cylinders.
 */
interface PipeMeshScope {
    /**
     * Declares an open-ended sequence of connected pipe segments.
     *
     * @param block configuration block for the pipe sequence nodes
     */
    fun sequence(block: PipeMeshSequenceScope.() -> Unit)

    /**
     * Declares a closed loop of connected pipe segments.
     *
     * @param block configuration block for the pipe sequence nodes
     */
    fun cycle(block: PipeMeshSequenceScope.() -> Unit)
}

/**
 * Scope for declaring individual nodes within a pipe sequence or loop.
 */
interface PipeMeshSequenceScope {
    /**
     * Adds a node to the pipe path.
     *
     * @param position 3D position of the node
     * @param radius thickness radius of the pipe at this node
     */
    fun node(position: Vec3, radius: Float)
}