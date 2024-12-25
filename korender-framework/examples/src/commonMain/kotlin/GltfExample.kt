package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.OrbitCamera
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun GltfExample() = Korender (appResourceLoader = { Res.readBytes(it) }) {
    val orbitCamera = OrbitCamera(20.z, 0.y)
    OnTouch { orbitCamera.touch(it) }
    Frame {
        Camera(orbitCamera.camera(projection, width, height))
        Scene(gltfResource = "!gltf/woman/meshy-woman.glb")
    }
}