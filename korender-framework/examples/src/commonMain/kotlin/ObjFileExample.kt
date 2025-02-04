package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.OrbitCamera
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.FloatMath.PIdiv2
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ObjFileExample() {
    Korender(appResourceLoader = { Res.readBytes(it) }) {
        val orbitCamera = OrbitCamera(this, 20.z, 0.z)
        OnTouch { orbitCamera.touch(it) }
        Frame {
            DirectionalLight(Vec3(1.0f, -1.0f, -1.0f).normalize(), white(3f))
            camera = orbitCamera.camera(projection, width, height)
            Renderable(
                standart {
                    baseColorTexture = texture("model/head.jpg")
                    pbr.metallic = 0.3f
                    pbr.roughness = 0.5f
                },
                mesh = obj("model/head.obj"),
                transform = scale(7.0f).rotate(1.y, -PIdiv2)
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