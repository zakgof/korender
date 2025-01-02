package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.OrbitCamera
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun GltfExample() = Korender (appResourceLoader = { Res.readBytes(it) }) {
    val orbitCamera = OrbitCamera(20.z, 2.y)
    OnTouch { orbitCamera.touch(it) }
    Light(Vec3(1.0f, -1.0f, -1.0f).normalize(), Color(1.0f, 7.0f, 7.0f, 7.0f))
    Ambient(Color(1.0f, 0.6f, 0.6f, 0.6f))
    Frame {
        Camera(orbitCamera.camera(projection, width, height))
        Scene(gltfResource = "gltf/ai/swat.glb")
        Gui {
            Filler()
            Text(id = "fps", fontResource = "font/orbitron.ttf", height = 50, text = "FPS ${frameInfo.avgFps}", color = Color(0xFF66FF55))
        }
    }
}