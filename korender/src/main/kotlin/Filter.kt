package com.zakgof.korender

import com.zakgof.korender.gpu.Gpu
import com.zakgof.korender.gpu.GpuShader
import com.zakgof.korender.material.Shaders
import com.zakgof.korender.material.UniformSupplier

class Filter(gpu: Gpu, fragmentShaderFile: String, val uniforms: UniformSupplier) {
    val gpuShader : GpuShader = Shaders.create(gpu,"screen.vert", fragmentShaderFile)
}