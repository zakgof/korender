package com.zakgof.korender

import com.zakgof.korender.gpu.GpuShader
import com.zakgof.korender.material.ShaderBuilder
import com.zakgof.korender.material.UniformSupplier

class Filter(gpu: Gpu, private val fragmentShaderFile: String, val uniforms: UniformSupplier, width: Int = 0, height: Int = 0) {

    val gpuShader : GpuShader

    init {
        gpuShader = ShaderBuilder("filter.vert", fragmentShaderFile).build(gpu)
    }

}