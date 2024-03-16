package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.Renderables
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.material.Materials
import com.zakgof.korender.math.Color
import com.zakgof.korender.projection.FrustumProjection

@Composable
fun QuickStartExample() = Korender {
    onResize = {
        projection =
            FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }
    add(
        Renderables.create(
            Meshes.sphere(2.0f).build(gpu),
            Materials.standard(gpu, "COLOR") {
                color = Color(0.2f, 1.0f, 0.5f)
            })
    )
}