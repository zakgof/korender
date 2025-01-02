package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.OrbitCamera
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.FloatMath.PIdiv2
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ObjFileExample() {
    val orbitCamera = OrbitCamera(20.z, 0.z)
    Korender(appResourceLoader = { Res.readBytes(it) }) {
        OnTouch { orbitCamera.touch(it) }
        Light(Vec3(1.0f, -1.0f, -1.0f).normalize(), Color(1.0f, 7.0f, 7.0f, 7.0f))
        Ambient(Color(1.0f, 0.3f, 0.3f, 0.3f))
        Frame {
            Camera(orbitCamera.camera(projection, width, height))
            Renderable(
                standart {
                    baseColorTexture = texture("model/head.jpg")
                    pbr.metallic = 0.1f
                    pbr.roughness = 0.5f
                },
                mesh = obj("model/head.obj"),
                transform = scale(7.0f).rotate(1.y, -PIdiv2)
            )
        }
    }
}