package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MetallicRoughnessExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    val freeCamera = FreeCamera(this, Vec3.ZERO, -1.z)
    OnTouch { freeCamera.touch(it) }
    val env = cubeTexture("cube/nx.jpg", "cube/ny.jpg", "cube/nz.jpg", "cube/px.jpg", "cube/py.jpg", "cube/pz.jpg")
    Frame {
        projection = frustum(width = 5f * width / height, height = 5f, near = 5f, far = 1000f)
        camera = freeCamera.camera(projection, width, height, 0f)
        Sky(cubeSky(env))
        DirectionalLight(Vec3(1.0f, -1.0f, 0.0f), ColorRGB.white(3f))
        DirectionalLight(Vec3(-1.0f, 1.0f, 0.0f), ColorRGB.white(0.2f))
        AmbientLight(ColorRGB.Black)
        for (m in 0..4) {
            for (r in 0..4) {
                Renderable(
                    standart {
                        baseColor = ColorRGBA.White
                        pbr.metallic = (r / 4.0f)
                        pbr.roughness = (m / 4.0f)
                    },
                    ibl(env),
                    mesh = sphere(0.8f),
                    transform = translate((m - 2) * 1.7f, (r - 2) * 1.7f, -10f)
                )
            }
        }
        Gui {
            Column {
                Filler()
                Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
            }
        }
    }
}