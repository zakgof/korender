package com.zakgof.korender.impl.engine

import com.zakgof.korender.declaration.UniformSupplier
import com.zakgof.korender.impl.geometry.Mesh
import com.zakgof.korender.impl.gpu.GpuShader
import com.zakgof.korender.impl.material.MapUniformSupplier
import com.zakgof.korender.math.Transform

internal class Renderable(val mesh: Mesh, val shader: GpuShader, val uniforms: UniformSupplier, val transform: Transform = Transform()) {
    fun render(uniformDecorator: (UniformSupplier) -> UniformSupplier) =
        shader.render(
            uniformDecorator(uniforms + MapUniformSupplier("model" to transform.mat4())),
            mesh.gpuMesh
        )
}
