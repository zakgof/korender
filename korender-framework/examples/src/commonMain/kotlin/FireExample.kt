package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.Renderables
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.material.Materials
import com.zakgof.korender.material.Shaders
import com.zakgof.korender.material.StockUniforms
import com.zakgof.korender.projection.FrustumProjection

@Composable
fun FireExample() = Korender {

    onResize = {
        projection =
            FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    val mesh = Meshes.billboard().build(gpu)
    val shader = Shaders.create(gpu, "billboard.vert", "fire.frag")
    val material = Materials.create(shader, StockUniforms(gpu).apply {
        xscale = 2.0f
        yscale = 10.0f
        static("strength", 4.0f)
    })

    add(Renderables.create(mesh, material))

}