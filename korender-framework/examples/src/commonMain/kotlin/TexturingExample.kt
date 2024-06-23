package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.declaration.MaterialModifiers.options
import com.zakgof.korender.declaration.MaterialModifiers.standardUniforms
import com.zakgof.korender.declaration.Meshes.sphere
import com.zakgof.korender.declaration.StandardMaterialOption
import com.zakgof.korender.declaration.Textures.texture
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

@Composable
fun TexturingExample(): Unit = Korender {
    Frame {
        Camera(DefaultCamera(position = 20.z, direction = -1.z, up = 1.y))
        Renderable(
            standardUniforms {
                colorTexture = texture("/sand.jpg")
            },
            mesh = sphere(2f),
            transform = Transform().rotate(1.y, frameInfo.time * 0.1f).translate(-2.1f.x)
        )
        Renderable(
            options(StandardMaterialOption.Triplanar),
            standardUniforms {
                colorTexture = texture("/sand.jpg")
                triplanarScale = 0.1f
            },
            mesh = sphere(2f),
            transform = Transform().rotate(1.y, frameInfo.time * 0.1f).translate(2.1f.x)
        )
    }
}