package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.OrbitCamera
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.FloatMath.PIdiv2
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

@Composable
fun ObjModelExample() {
    Korender(resourceLoader = { Res.readBytes("files/$it") }) {
        val orbitCamera = OrbitCamera(20.z, 0.z)
        OnTouch { orbitCamera.touch(it) }
        Frame {
            DirectionalLight(Vec3(1.0f, -1.0f, -1.0f), white(3f))
            camera = orbitCamera.run { camera() }
            Model(
                resource = "model/head.obj",
                instancing = modelInstancing("2", 2, false) {
                    Instance(transform = scale(1.0f).rotate(1.y, -PIdiv2).translate(-2.x))
                    Instance(transform = scale(1.0f).rotate(1.y, -PIdiv2).translate(2.x))
                },
            )
            Gui {
                Column {
                    Filler()
                    Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
                }
            }
            TestExchange.report(frameInfo)
        }
    }
}
