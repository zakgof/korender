package com.zakgof.korender

import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.gpu.Gpu
import com.zakgof.korender.material.Materials
import com.zakgof.korender.material.Shaders
import com.zakgof.korender.material.UniformSupplier

object Skies {

    fun create(gpu: Gpu, fragShader: String, uniforms: UniformSupplier = UniformSupplier { null }) =
        SimpleRenderable(
            Meshes.screenQuad().build(gpu),
            Materials.create(
                Shaders.create(gpu, "sky.vert", fragShader), uniforms
            ),
            true
        )

    fun fastClouds(gpu: Gpu) = create(gpu, "fastcloudsky.frag")

    fun fancyClouds(gpu: Gpu) = create(gpu, "cloudsky.frag")

    fun stars(gpu: Gpu) = create(gpu, "starsky.frag")
}