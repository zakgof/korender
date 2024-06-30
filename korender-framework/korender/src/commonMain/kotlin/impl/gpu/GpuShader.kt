package com.zakgof.korender.impl.gpu

import com.zakgof.korender.uniforms.UniformSupplier

interface GpuShader : AutoCloseable {
    fun render(uniformSupplier: UniformSupplier, mesh: GpuMesh)
}
