package com.zakgof.korender.gpu

import com.zakgof.korender.material.UniformSupplier

interface GpuShader {
    fun render(uniformSupplier: UniformSupplier, mesh: GpuMesh)
}
