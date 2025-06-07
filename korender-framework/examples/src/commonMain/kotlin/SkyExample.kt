package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi

@Composable
fun SkyExample() {
    Korender(appResourceLoader = { Res.readBytes(it) }) {
        val freeCamera = FreeCamera(this, Vec3(0f, 4f, 20f), -1.z)
        OnTouch { freeCamera.touch(it) }
        Frame {
            AmbientLight(ColorRGB.White)
            camera = freeCamera.camera(projection, width, height, 0f)
            Sky(starrySky())
            Renderable(
                base(colorTexture = texture("texture/asphalt-albedo.jpg"), metallicFactor = 0.2f, roughnessFactor = 0.9f),
                normalTexture(normalTexture = texture("texture/asphalt-normal.jpg")),
                triplanar(0.1f),
                mesh = cube(1f),
                transform = scale(2000f, 1f, 2000f)
            )
            Gui {
                Column {
                    Filler()
                    Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
                }
            }
        }
    }
}
