package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform.Companion.rotate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.random.Random

@OptIn(ExperimentalResourceApi::class)
@Composable
fun InstancedCubesExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    val freeCamera = FreeCamera(this, 20.z, -1.z)
    OnTouch { freeCamera.touch(it) }
    Frame {
        DirectionalLight(Vec3(1f, 1f, -2f), ColorRGB.White)
        camera = freeCamera.camera(projection, width, height, frameInfo.dt)
        InstancedRenderables(
            base(colorTexture = texture("texture/asphalt-albedo.jpg"), metallicFactor = 0.1f),
            id = "particles",
            count = 21 * 21,
            mesh = cube(0.3f)
        ) {
            for (x in -10..10) {
                for (y in -10..10) {
                    val random = Random(x + 100 * y)
                    val axis = Vec3(random.nextFloat(), random.nextFloat(), random.nextFloat()).normalize()
                    Instance(
                        transform = rotate(Quaternion.fromAxisAngle(axis, frameInfo.time * 3f))
                            .translate(Vec3(x.toFloat(), y.toFloat(), 0f))
                    )
                }
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