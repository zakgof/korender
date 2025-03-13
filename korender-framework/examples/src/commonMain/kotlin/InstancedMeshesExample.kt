package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun InstancedMeshesExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    val freeCamera = FreeCamera(this, 20.z, -1.z)
    OnTouch { freeCamera.touch(it) }
    Frame {
        DirectionalLight(Vec3(1f, 1f, -2f), ColorRGB.White)
        camera = freeCamera.camera(projection, width, height, frameInfo.dt)
        InstancedRenderables(
            standart {
                baseColorTexture = texture("texture/asphalt-albedo.jpg")
                pbr.metallic = 0.1f
            },
            id = "particles",
            count = 21 * 21,
            mesh = cube(0.4f),
            static = true
        ) {
            for (x in -10..10) {
                for (y in -10..10) {
                    Instance(transform = translate(Vec3(x.toFloat(), y.toFloat(), 0f)))
                }
            }
        }
        Gui {
            Row {
                Filler()
                Column {
                    Filler()
                    Image(
                        imageResource = "texture/accelerate.png",
                        width = 128,
                        height = 128,
                        onTouch = { freeCamera.forward(it) })
                    Image(
                        imageResource = "texture/decelerate.png",
                        width = 128,
                        height = 128,
                        onTouch = { freeCamera.backward(it) })
                }
            }
        }
    }
}