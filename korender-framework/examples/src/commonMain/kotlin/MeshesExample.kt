package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.OrbitCamera
import com.zakgof.korender.math.ColorRGB.Companion.White
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import kotlin.math.sin

@Composable
fun MeshesExample() {
    Korender(appResourceLoader = { Res.readBytes(it) }) {
        val orbitCamera = OrbitCamera(20.z, 2.y)
        OnTouch { orbitCamera.touch(it) }
        val material = base { color = ColorRGBA.Blue }
        Frame {
            AmbientLight(White)
            camera = orbitCamera.run { camera() }
            Renderable(
                material,
                mesh = customMesh(
                    id = "static",
                    vertexCount = 3,
                    indexCount = 3,
                    POS, NORMAL, TEX,
                ) {
                    pos(-5.x).normal(1.z).tex(Vec2(0f, 0f))
                    pos(0.z).normal(1.z).tex(Vec2(1f, 0f))
                    pos(5.y).normal(1.z).tex(Vec2(1f, 1f))
                    index(0, 1, 2)
                }
            )
            Renderable(
                material,
                mesh = customMesh(
                    id = "dynamic",
                    vertexCount = 3,
                    indexCount = 3,
                    POS, NORMAL, TEX,
                    dynamic = true
                ) {
                    pos(1.x).normal(1.z).tex(Vec2(0f, 0f))
                    pos(5.x).normal(1.z).tex(Vec2(1f, 0f))
                    pos(Vec3(5f, 5f + sin(frameInfo.time), 0f)).normal(1.z).tex(Vec2(1f, 1f))
                    index(0, 1, 2)
                }
            )
        }
    }
}
