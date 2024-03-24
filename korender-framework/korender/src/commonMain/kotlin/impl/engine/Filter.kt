package com.zakgof.korender.impl.engine

import com.zakgof.korender.declaration.UniformSupplier
import com.zakgof.korender.impl.gpu.GpuShader

class Filter(val gpuShader: GpuShader, val uniforms: UniformSupplier)