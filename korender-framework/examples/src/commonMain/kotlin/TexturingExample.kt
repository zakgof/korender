package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.declaration.MeshDeclarations.sphere
import com.zakgof.korender.material.Materials.standard
import com.zakgof.korender.material.Textures.texture
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import com.zakgof.korender.projection.FrustumProjection

@Composable
fun TexturingExample(): Unit = Korender {

    camera = DefaultCamera(position = 20.z, direction = -1.z, up = 1.y)
    onResize = {
        projection =
            FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    Scene {
        Renderable(
            mesh = sphere(2f),
            material = standard {
                colorTexture = texture("/sand.jpg")
            },
            transform = Transform().rotate(1.y, frameInfo.time * 0.1f).translate(-2.1f.x)
        )
        Renderable(
            mesh = sphere(2f),
            material = standard("TRIPLANAR") {
                colorTexture = texture("/sand.jpg")
                triplanarScale = 0.1f
            },
            transform = Transform().rotate(1.y, frameInfo.time * 0.1f).translate(2.1f.x)
        )
    }
}