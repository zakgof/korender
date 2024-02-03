package com.zakgof.korender

import com.zakgof.korender.gpu.GpuShader
import com.zakgof.korender.gpu.GpuMesh

data class Renderable(val gpuMesh: GpuMesh, val gpuShader: GpuShader, val uniformSupplier: UniformSupplier) {

    fun render() = gpuShader.render(uniformSupplier, gpuMesh)

}
