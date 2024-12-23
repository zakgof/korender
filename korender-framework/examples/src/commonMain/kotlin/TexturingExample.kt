package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.material.MaterialModifiers.standart
import com.zakgof.korender.material.StandartMaterialOption
import com.zakgof.korender.math.Transform.Companion.rotate
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import com.zakgof.korender.mesh.Meshes.sphere
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun TexturingExample(): Unit = Korender(appResourceLoader = { Res.readBytes(it) }) {
    Frame {
        Camera(DefaultCamera(position = 20.z, direction = -1.z, up = 1.y))
        Renderable(
            standart {
                colorTexture = texture("!sand.jpg")
            },
            mesh = sphere(2f),
            transform = rotate(1.y, frameInfo.time * 0.1f).translate(-2.1f.x)
        )
        Renderable(
            standart(StandartMaterialOption.Triplanar) {
                colorTexture = texture("!sand.jpg")
                triplanarScale = 0.1f
            },
            mesh = sphere(2f),
            transform = rotate(1.y, frameInfo.time * 0.1f).translate(2.1f.x)
        )
    }
}