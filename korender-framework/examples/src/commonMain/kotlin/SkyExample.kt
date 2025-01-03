package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.sin

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SkyExample() {
    Korender(appResourceLoader = { Res.readBytes(it) }) {
        val freeCamera = FreeCamera(this, 20.z, -1.z)
        OnTouch { freeCamera.touch(it) }
        Frame {
            camera = freeCamera.camera(projection, width, height, 0f)
            Sky(fastCloudSky() {
                thickness = 10f + 10f * sin(frameInfo.time * 0.5f)
            })
            Gui {
                Filler()
                Text(id = "fps", fontResource = "font/orbitron.ttf", height = 50, text = "FPS ${frameInfo.avgFps}", color = Color(0xFF66FF55))
            }
        }
    }
}
