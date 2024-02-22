package com.zakgof.korender

import com.zakgof.korender.geometry.Mesh
import com.zakgof.korender.material.MapUniformSupplier
import com.zakgof.korender.material.Material
import com.zakgof.korender.material.UniformSupplier
import com.zakgof.korender.math.BoundingBox
import com.zakgof.korender.math.Transform

interface Renderable {

    val material: Material
    val transform: Transform
    val mesh: Mesh
    val worldBoundingBox: BoundingBox?

    fun render(contextUniforms: UniformSupplier) =
        material.gpuShader.render(
            material.uniforms + MapUniformSupplier("model" to transform.mat4()) + contextUniforms,
            mesh.gpuMesh
        )
}
