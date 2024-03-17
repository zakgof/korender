package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.material.Materials.standard
import com.zakgof.korender.declaration.MeshDeclarations.sphere
import com.zakgof.korender.projection.FrustumProjection

@Composable
fun FilterExample() = Korender {

    onResize = {
        projection =
            FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    Scene {
        Renderable(
            mesh = sphere(2.2f),
            material = standard {
                colorFile = "/sand.jpg"
            }
        )
        Filter(fragment = "bw.frag")
    }

}