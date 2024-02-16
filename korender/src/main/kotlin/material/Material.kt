package com.zakgof.korender.material

import com.zakgof.korender.gpu.GpuShader

interface Material {
    val gpuShader: GpuShader
    val uniforms: UniformSupplier
}