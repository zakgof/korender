package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.sin

@OptIn(ExperimentalResourceApi::class)
@Composable
fun HeightFieldExample() {
    Korender(appResourceLoader = { Res.readBytes(it) }) {
        val freeCamera = FreeCamera(this, 10.y, Vec3(0f, -1f, -1f).normalize())
        Frame {
            OnTouch { freeCamera.touch(it) }
            camera = freeCamera.camera(projection, width, height, frameInfo.dt)
            Renderable(
                standart {
                    baseColorTexture = texture("sand.jpg")
                    pbr.metallic = 0.6f
                },
                mesh = heightField(id = "terrain", 128, 128, 0.2f) { x, y ->
                    0.5f * (sin(x * 0.2f) + sin(y * 0.2f))
                }
            )
        }
    }
}