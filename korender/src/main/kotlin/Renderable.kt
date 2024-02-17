package com.zakgof.korender

import com.zakgof.korender.geometry.Mesh
import com.zakgof.korender.material.Material
import com.zakgof.korender.material.UniformSupplier
import com.zakgof.korender.math.BoundingBox
import com.zakgof.korender.math.Transform

data class Renderable(val mesh: Mesh, val material: Material) {
    var worldBoundingBox: BoundingBox? = null
    var transform: Transform = Transform()
        set(value) {
            field = value
            worldBoundingBox = updateBB()
        }

    init {
        worldBoundingBox = updateBB()
    }

    private fun updateBB() = mesh.modelBoundingBox?.transform(transform)

    fun render(contextUniforms: UniformSupplier) =
        material.gpuShader.render({
            material.uniforms[it] ?: mapOf("model" to transform.mat4())[it] ?: contextUniforms[it]
        }, mesh.gpuMesh)
}
