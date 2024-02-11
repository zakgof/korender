package com.zakgof.korender

import com.zakgof.korender.gpu.GpuMesh
import com.zakgof.korender.gpu.GpuShader
import com.zakgof.korender.material.UniformSupplier
import com.zakgof.korender.math.Transform

data class Renderable(
    private val gpuMesh: GpuMesh,
    private val gpuShader: GpuShader,
    private val uniforms: UniformSupplier
) {
    var transform: Transform = Transform()

    fun render() =
        gpuShader.render(UniformSupplier { uniforms[it] ?: mapOf("model" to transform.mat4())[it] }, gpuMesh)

}
