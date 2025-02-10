package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun IblExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    val freeCamera = FreeCamera(this, Vec3.ZERO, -1.z)
    OnTouch { freeCamera.touch(it) }
    Frame {
        CaptureEnv(0, 512, Vec3.ZERO, 1f, 100f) {
            scene()
        }
        camera = freeCamera.camera(projection, width, height, 0f)
        if (fract(frameInfo.time * 0.5f) < 0.5f) {
            Sky(cubeSky(0))
        } else {
            scene()
        }
    }
}

private fun FrameContext.scene() {
    AmbientLight(white(0.5f))
    DirectionalLight(Vec3(1.0f, -1.0f, -1.0f).normalize())
    Sky(fastCloudSky())
    Renderable(
        standart {
            baseColor = ColorRGBA.Red
        },
        mesh = sphere(0.5f),
        transform = translate(2.y - 15.z)
    )
}