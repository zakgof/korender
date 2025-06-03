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
import kotlin.random.Random

@Composable
fun InstancedCubesExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    val freeCamera = FreeCamera(this, 50.z, -1.z)
    OnTouch { freeCamera.touch(it) }
    Frame {
        DirectionalLight(Vec3(1f, 1f, -2f), ColorRGB.White)
        camera = freeCamera.camera(projection, width, height, frameInfo.dt)

        Renderable(
            base(colorTexture = texture("texture/asphalt-albedo.jpg"), metallicFactor = 0.1f),
            mesh = cube(0.3f),
            instancing = instancing(
                id = "particles",
                count = 41 * 41,
                dynamic = true
            ) {
                for (x in -20..20) {
                    for (y in -20..20) {
                        val random = Random(x + 100 * y)
                        val axis = Vec3(random.nextFloat(), random.nextFloat(), random.nextFloat()).normalize()
                        Instance(
                            transform = rotate(Quaternion.fromAxisAngle(axis, frameInfo.time * 3f))
                                .translate(Vec3(x.toFloat(), y.toFloat(), 0f))
                        )
                    }
                }
            }
        )

        Gui {
            Column {
                Filler()
                Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
            }
        }
    }
}