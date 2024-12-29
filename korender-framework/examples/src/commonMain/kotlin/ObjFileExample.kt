package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.OrbitCamera
import com.zakgof.korender.material.MaterialModifiers.standart
import com.zakgof.korender.material.StandartMaterialOption
import com.zakgof.korender.math.FloatMath
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ObjFileExample() {
    val orbitCamera = OrbitCamera(20.z, 0.z)
    Korender(appResourceLoader = { Res.readBytes(it) }) {
        OnTouch { orbitCamera.touch(it) }
        Frame {
            Camera(orbitCamera.camera(projection, width, height))
            Renderable(
                standart(StandartMaterialOption.AlbedoMap) {
                    albedoTexture = texture("!model/head.jpg")
                },
                mesh = obj("!model/head.obj"),
                transform = scale(7.0f).rotate(1.y, -FloatMath.PIdiv2)
            )
        }
    }
}