package com.zakgof.korender.gpu

import com.zakgof.korender.UniformSupplier

interface GpuShader {
    fun render(uniformSupplier: UniformSupplier, mesh: GpuMesh)
}
