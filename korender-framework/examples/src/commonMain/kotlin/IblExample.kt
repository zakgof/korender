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
//        CaptureEnv(0) {
//            rends()
//        }
        projection = frustum(width = 5f * width / height, height = 5f, near = 5f, far = 1000f)
        camera = freeCamera.camera(projection, width, height, 0f)
        rends()
        Sky(cubeSky(cubeTexture("cube/nx.jpg", "cube/ny.jpg", "cube/nz.jpg", "cube/px.jpg", "cube/py.jpg", "cube/pz.jpg")))
//        if (fract(frameInfo.time * 0.2f) < 0.5f) {
//            Filter(fragment("!shader/effect/env-debug.frag"))
//        }
    }
}

private fun FrameContext.rends() {
    Sky(fastCloudSky())
    AmbientLight(white(0.5f))
    DirectionalLight(Vec3(1.0f, -1.0f, -1.0f).normalize())
//    Renderable(
//        standart {
//            baseColorTexture = texture("texture/asphalt-albedo.jpg")
//        },
//        mesh = cube(),
//        transform = scale(1000f, 1f, 1000f).translate(-3.y)
//    )
    Renderable(
        standart {
            baseColor = ColorRGBA.Red
        },
        mesh = sphere(0.5f),
        transform = translate(2.y - 10.z)
    )
}