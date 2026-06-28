package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.OrbitCamera
import com.zakgof.korender.math.ColorRGB.Companion.White
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.z

@Composable
fun StochasticTexturingExample() {
    Korender(resourceLoader = { Res.readBytes("files/$it") }) {
        val orbitCamera = OrbitCamera(20.z, 0.z)
        val plane = customMesh("plane", 4, 6, POS, TEX) {
            pos(Vec3(-2f, -2f, 0f)).tex(Vec2(0f, 0f))
            pos(Vec3( 2f, -2f, 0f)).tex(Vec2(5f, 0f))
            pos(Vec3( 2f,  2f, 0f)).tex(Vec2(5f, 5f))
            pos(Vec3(-2f,  2f, 0f)).tex(Vec2(0f, 5f))
            index(0, 1, 2, 0, 2, 3)
        }
        OnTouch { orbitCamera.touch(it) }
        Frame {
            AmbientLight(White)
            camera = orbitCamera.run { camera() }
            Renderable(
                base {
                    colorTexture = texture("texture/brick.jpg")
                    metallicFactor = 0.3f
                    roughnessFactor = 0.5f
                    stochasticSharpness = 12f
                },
                mesh = plane,
                transform = translate(-3.x)
            )
            Renderable(
                base {
                    colorTexture = texture("texture/brick.jpg")
                    metallicFactor = 0.3f
                    roughnessFactor = 0.5f
                    stochasticSharpness = 12f
                },
                mesh = plane,
                transform = translate(3.x)
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
