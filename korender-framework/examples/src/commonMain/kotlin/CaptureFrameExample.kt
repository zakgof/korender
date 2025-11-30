package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.math.ColorRGB.Companion.White
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun CaptureFrameExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    val frame = captureFrame(256, 256,
        camera(-40.z, 1.z, 1.y),
        projection(5f, 5f, 5f, 100f)) {
        frameScene()
    }
    val freeCamera = FreeCamera(this, -40.z, 1.z)
    OnTouch { freeCamera.touch(it) }
    Frame {
        camera = freeCamera.camera(projection, width, height, frameInfo.dt)

        if (fract(frameInfo.time * 0.2f) > 0.5f && frame.isCompleted) {
            AmbientLight(White)
            Billboard(
                billboard(
                    position = 30.z,
                    scale = Vec2(10f, 10f)
                ),
                base (
                    metallicFactor = 0.0f,
                    roughnessFactor = 1.0f,
                    colorTexture = texture("captured", frame.getCompleted())
                )
            )
        } else {
            frameScene()
        }

        Gui {
            Column {
                Filler()
                Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}", height = 40, color = ColorRGBA(0x66FF55A0))
            }
        }
    }
}

fun FrameContext.frameScene() {
    DirectionalLight(Vec3(1f, -1f, 1f))
    val rnd = Random(1)
    for (i in 0 until 1000) {
        Renderable(
            base(color = ColorRGBA(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat(), 1f)),
            mesh = sphere(),
            transform = scale(1f * rnd.nextFloat()).translate(Vec3.random(i) * 10f)
        )
    }
}
