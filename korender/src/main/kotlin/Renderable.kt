package com.zakgof.korender

import com.zakgof.korender.geometry.Mesh
import com.zakgof.korender.gpu.GpuShader
import com.zakgof.korender.material.UniformSupplier
import com.zakgof.korender.math.BoundingBox
import com.zakgof.korender.math.Transform

data class Renderable(
    val mesh: Mesh,
    val gpuShader: GpuShader,
    val uniforms: UniformSupplier
) {
    var worldBoundingBox: BoundingBox? = null
    var transform: Transform = Transform()
        set(value) {
            field = value
            worldBoundingBox = updateBB()
        }

    init {
        worldBoundingBox = updateBB()
    }

    private fun updateBB(): BoundingBox? {
        mesh.modelBoundingBox?.let {
            return it.transform(transform)
        }
        return null
    }

    fun render(contextUniforms: UniformSupplier) =
        gpuShader.render( { uniforms[it] ?: mapOf("model" to transform.mat4())[it] ?: contextUniforms[it] }, mesh.gpuMesh)

}
