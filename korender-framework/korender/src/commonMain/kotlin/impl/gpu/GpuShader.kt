package com.zakgof.korender.impl.gpu

import com.zakgof.korender.material.UniformSupplier

interface GpuShader : AutoCloseable {
    fun render(uniformSupplier: UniformSupplier, mesh: GpuMesh)
}
