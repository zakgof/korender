package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Filter
import com.zakgof.korender.Korender
import com.zakgof.korender.Renderables
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.material.Materials
import com.zakgof.korender.projection.FrustumProjection

@Composable
fun FilterExample() = Korender {

    onResize = {
        projection =
            FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    val renderable = Renderables.create(
        Meshes.sphere(2.2f).build(gpu),
        Materials.standard(gpu) {
            colorFile = "/sand.jpg"
        });
    add(renderable)
    addFilter(Filter(gpu, "bw.frag"))
}