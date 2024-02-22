package com.zakgof.korender

import com.zakgof.korender.geometry.Mesh
import com.zakgof.korender.material.Material
import com.zakgof.korender.math.BoundingBox
import com.zakgof.korender.math.Transform

class SimpleRenderable(override val mesh: Mesh, override val material: Material) : Renderable {

    override var worldBoundingBox: BoundingBox? = null
    override var transform: Transform = Transform()
        set(value) {
            field = value
            worldBoundingBox = updateBB()
        }

    init {
        worldBoundingBox = updateBB()
    }

    private fun updateBB() = mesh.modelBoundingBox?.transform(transform)

}
