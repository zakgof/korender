package com.zakgof.korender

import com.zakgof.korender.gpu.GpuShader
import com.zakgof.korender.material.ShaderBuilder
import com.zakgof.korender.material.UniformSupplier

class Filter(gpu: Gpu, fragmentShaderFile: String, val uniforms: UniformSupplier) {
    val gpuShader : GpuShader = ShaderBuilder("screen.vert", fragmentShaderFile).build(gpu)
}