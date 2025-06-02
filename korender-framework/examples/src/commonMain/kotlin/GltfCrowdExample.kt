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
fun GltfCrowdExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    val orbitCamera = OrbitCamera(this, 15.z, 0.y)
    OnTouch { orbitCamera.touch(it) }
    Frame {
        camera = orbitCamera.camera(projection, width, height)
        AmbientLight(white(0.6f))
        DirectionalLight(Vec3(1.0f, -1.0f, -1.0f), white(3f))
        Gltf(
            resource = "gltf/ai/swat.glb",
            instancing = gltfInstancing("crowd", 9, true) {
                (-1..1).forEach { x ->
                    (-1..1).forEach { z ->
                        Instance(
                            time = (x + z * 31) * 100f + frameInfo.time,
                            transform = scale(0.01f).translate(x.toFloat() * 2f, -3f, z.toFloat() * 2f)
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