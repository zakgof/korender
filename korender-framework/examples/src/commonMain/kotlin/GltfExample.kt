package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.OrbitCamera
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

@Composable
fun GltfExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    val orbitCamera = OrbitCamera(this, 20.z, 2.y)
    OnTouch { orbitCamera.touch(it) }
    Frame {
        camera = orbitCamera.camera(projection, width, height)
        DirectionalLight(Vec3(1.0f, -1.0f, -1.0f), white(3f))
        AmbientLight(white(0.6f))
        Gltf(resource = "gltf/ai/swat.glb", transform = scale(0.03f).rotate(1.y, frameInfo.time))
        Gui {
            Column {
                Filler()
                Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
            }
        }
    }
}