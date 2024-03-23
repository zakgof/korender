package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.FrameInfo
import com.zakgof.korender.Korender
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.z
import com.zakgof.korender.projection.FrustumProjection

@Composable
fun SkyExample() {

    Korender {

        val freeCamera = FreeCamera(20.z, -1.z)
        OnTouch { freeCamera.touch(it) }

        Scene {
            projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
            camera = freeCamera.camera(korenderContext = korenderContext, 0f)

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