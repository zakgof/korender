package com.zakgof.korender

import com.zakgof.korender.gpu.GpuShader
import com.zakgof.korender.material.UniformSupplier

class Filter(val gpuShader: GpuShader, val uniforms: UniformSupplier)