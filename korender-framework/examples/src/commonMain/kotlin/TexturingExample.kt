package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.material.MaterialModifiers.options
import com.zakgof.korender.material.MaterialModifiers.standartUniforms
import com.zakgof.korender.mesh.Meshes.sphere
import com.zakgof.korender.material.StandartMaterialOption
import com.zakgof.korender.material.Textures.texture
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

@Composable
fun TexturingExample(): Unit = Korender {
    Frame {
        Camera(DefaultCamera(position = 20.z, direction = -1.z, up = 1.y))
        Renderable(
            standartUniforms {
                colorTexture = texture("/sand.jpg")
            },
            mesh = sphere(2f),
            transform = Transform().rotate(1.y, frameInfo.time * 0.1f).translate(-2.1f.x)
        )
        Renderable(
            options(StandartMaterialOption.Triplanar),
            standartUniforms {
                colorTexture = texture("/sand.jpg")
                triplanarScale = 0.1f
            },
            mesh = sphere(2f),
            transform = Transform().rotate(1.y, frameInfo.time * 0.1f).translate(2.1f.x)
        )
    }
}