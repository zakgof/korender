package com.zakgof.korender

import com.zakgof.korender.gpu.GpuMesh
import com.zakgof.korender.gpu.GpuShader
import com.zakgof.korender.material.UniformSupplier
import com.zakgof.korender.math.BoundingBox
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3

data class Renderable(
    private val gpuMesh: GpuMesh,
    private val gpuShader: GpuShader,
    private val uniforms: UniformSupplier
) {
    var modelBoundingBox: BoundingBox? = null
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
        modelBoundingBox?.let {
            return it.transform(transform)
        }
        return null
    }

    fun render() =
        gpuShader.render(UniformSupplier { uniforms[it] ?: mapOf("model" to transform.mat4())[it] }, gpuMesh)

}
