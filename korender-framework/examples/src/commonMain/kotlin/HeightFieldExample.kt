package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.material.MaterialModifiers.standart
import com.zakgof.korender.material.StandartMaterialOption
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.mesh.Meshes.heightField
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.sin

@OptIn(ExperimentalResourceApi::class)
@Composable
fun HeightFieldExample() {
    val freeCamera = FreeCamera(10.y, Vec3(0f, -1f, -1f).normalize())
    Korender(appResourceLoader = { Res.readBytes(it) }) {
        Frame {
            OnTouch { freeCamera.touch(it) }
            Camera(freeCamera.camera(projection, width, height, frameInfo.dt))
            Renderable(
                standart(StandartMaterialOption.AlbedoMap) {
                    albedoTexture = texture("!sand.jpg")
                },
                mesh = heightField(id = "terrain", 128, 128, 0.2f) { x, y ->
                    0.5f * (sin(x * 0.2f) + sin(y * 0.2f))
                }
            )
        }
    }
}