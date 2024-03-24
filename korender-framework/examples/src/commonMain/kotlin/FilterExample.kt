package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.declaration.Materials.standard
import com.zakgof.korender.declaration.Meshes.sphere
import com.zakgof.korender.material.Textures.texture

@Composable
fun FilterExample() = Korender {
    Scene {
        Renderable(
            mesh = sphere(2.2f),
            material = standard {
                colorTexture = texture("/sand.jpg")
            }
        )
        Filter(fragment = "bw.frag")
    }
}