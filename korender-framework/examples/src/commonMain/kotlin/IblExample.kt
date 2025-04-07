package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun IblExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    val freeCamera = FreeCamera(this, Vec3.ZERO, -1.z)
    OnTouch { freeCamera.touch(it) }
    projection = frustum(3f * width / height, 3f, 3f, 100f)
    Frame {

//        camera = camera(80.z, -1.z, 1.y)
//        scene()
//        return@Frame

        CaptureEnv(0, 512, Vec3.ZERO, 1f, 100f, insideOut = true) {
            scene()
        }
        camera = freeCamera.camera(projection, width, height, frameInfo.dt)
        Billboard(
            standart {
                baseColorTexture = texture("texture/grass.jpg")
                xscale = 3.0f
                yscale = 3.0f
            },
            fragment("mpr/mpr.frag"),
            position = -4f.z)

        Sky(cubeSky(0))



        Gui {
            Column {
                Filler()
                Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
            }
        }
    }
}

private fun FrameContext.scene() {
    AmbientLight(white(0.5f))
    DirectionalLight(Vec3(1.0f, -1.0f, -1.0f).normalize())
    Renderable(
        standart {
            baseColor = ColorRGBA.Red
            baseColorTexture = texture("texture/grass.jpg")
        },
        mesh = sphere(10.0f),
        transform = Transform()
    )
//    Renderable(
//        standart {
//            baseColor = ColorRGBA.Green
//            baseColorTexture = texture("texture/grass.jpg")
//        },
//        mesh = sphere(10.0f),
//        transform = translate(-5.x)
//    )
}