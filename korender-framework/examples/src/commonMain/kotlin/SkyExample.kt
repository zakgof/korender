package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.FrameInfo
import com.zakgof.korender.Korender
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.z

@Composable
fun SkyExample() {

    Korender {

        val freeCamera = FreeCamera(20.z, -1.z)
        OnTouch { freeCamera.touch(it) }

        Scene {
            Camera(freeCamera.camera(projection, width, height, 0f))
            Sky(preset = preset(frameInfo))
            Gui {
                Filler()
                Text(id = "fps", fontResource = "/ubuntu.ttf", height = 50, text = "FPS ${frameInfo.avgFps}", color = Color(0x66FF55))
            }
        }
    }
}

private fun preset(frameInfo: FrameInfo) =
    when ((frameInfo.time * 0.1).toInt() % 3) {
        0 -> "fastcloud"
        1 -> "cloud"
        else -> "star"
    }