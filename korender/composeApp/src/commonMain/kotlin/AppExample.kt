package com.zakgof.korender

import androidx.compose.runtime.Composable
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.material.Materials
import com.zakgof.korender.projection.FrustumProjection

@Composable
fun AppExample() {
    Korender() {
        onResize = {
            projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
        }
        add(SimpleRenderable(
            mesh = Meshes.sphere(2f).build(gpu),
            material = Materials.standard(gpu) {
                colorFile = "/sand.jpg"
            }
        ))
    }
}