package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.OrbitCamera
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun GltfExample() = Korender (appResourceLoader = { Res.readBytes(it) }) {
    val orbitCamera = OrbitCamera(this, 20.z, 2.y)
    OnTouch { orbitCamera.touch(it) }
    Frame {
        camera = orbitCamera.camera(projection, width, height)
        DirectionalLight(Vec3(1.0f, -1.0f, -1.0f).normalize(), Color.white(3f))
        AmbientLight(Color.white(0.6f))
        Scene(gltfResource = "gltf/ai/swat.glb", transform = scale(0.03f).rotate(1.y, frameInfo.time))
        Gui {
            Filler()
            Text(id = "fps", fontResource = "font/orbitron.ttf", height = 30, text = "FPS ${frameInfo.avgFps.toInt()}", color = Color(0xFF66FF55))
        }
    }
}