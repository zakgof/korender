package com.zakgof.korender

import com.zakgof.korender.geometry.Mesh
import com.zakgof.korender.gpu.GpuShader
import com.zakgof.korender.material.MapUniformSupplier
import com.zakgof.korender.material.UniformSupplier
import com.zakgof.korender.math.Transform

class Renderable(
    val mesh: Mesh,
    val shader: GpuShader,
    val uniforms: UniformSupplier,
    val transform: Transform = Transform()
) {
    fun render(contextUniforms: UniformSupplier) =
        shader.render(
            uniforms + MapUniformSupplier("model" to transform.mat4()) + contextUniforms,
            mesh.gpuMesh
        )
}
