package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.z
import kotlin.random.Random

@Composable
fun CaptureEnvExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    val env = captureEnv(1024, 1f, 1000f) {
        scene()
    }
    val freeCamera = FreeCamera(this, 0.z, (-1).z)
    OnTouch { freeCamera.touch(it) }
    Frame {
        camera = freeCamera.camera(projection, width, height, frameInfo.dt)

        if (fract(frameInfo.time * 0.2f) > 0.5f) {
            Sky(cubeSky(cubeTexture("spheres", env)))
        } else {
            scene()
        }

        Gui {
            Column {
                Filler()
                Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}", height = 40, color = ColorRGBA(0x66FF55A0))
            }
        }
    }
}

fun FrameContext.scene() {
    DirectionalLight(Vec3(1f, -1f, 1f))
    val rnd = Random(1)
    for (i in 0 until 10000) {
        Renderable(
            base(color = ColorRGBA(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat(), 1f)),
            mesh = sphere(),
            transform = scale(1f + 1f * rnd.nextFloat()).translate(Vec3.random(i) * 100f)
        )
    }
}
