package com.zakgof.korender

import com.zakgof.korender.geometry.Mesh
import com.zakgof.korender.material.Material
import com.zakgof.korender.math.BoundingBox
import com.zakgof.korender.math.Transform

object Renderables {

    fun create(mesh: Mesh, material: Material, alwaysVisible: Boolean = false) =
        SimpleRenderable(mesh, material, alwaysVisible)
}


class SimpleRenderable(
    override val mesh: Mesh,
    override val material: Material,
    private val alwaysVisible: Boolean = false
) : Renderable {

    override var worldBoundingBox: BoundingBox? = null
    override var transform: Transform = Transform()
        set(value) {
            field = value
            if (!alwaysVisible) {
                worldBoundingBox = updateBB()
            }
        }

    init {
        if (!alwaysVisible) {
            worldBoundingBox = updateBB()
        }
    }

    private fun updateBB() = mesh.modelBoundingBox?.transform(transform)

}
