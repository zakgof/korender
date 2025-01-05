package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SkyExample() {
    Korender(appResourceLoader = { Res.readBytes(it) }) {
        val freeCamera = FreeCamera(this, Vec3(0f, 4f, 20f), -1.z)
        OnTouch { freeCamera.touch(it) }
        Frame {
            camera = freeCamera.camera(projection, width, height, 0f)
            Sky(starrySky())
            Renderable(
                standart {
                    baseColorTexture = texture("texture/asphalt-albedo.jpg")
                    normalTexture = texture("texture/asphalt-normal.jpg")
                    triplanarScale = 0.1f
                    pbr.metallic = 0.2f
                    pbr.roughness = 0.9f
                },
                mesh = cube(1f),
                transform = scale(200f, 1f, 200f)
            )
            Gui {
                Filler()
                Text(id = "fps", fontResource = "font/orbitron.ttf", height = 50, text = "FPS ${frameInfo.avgFps}", color = Color(0xFF66FF55))
            }
        }
    }
}
