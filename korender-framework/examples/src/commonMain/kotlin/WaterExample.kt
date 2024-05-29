package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.declaration.Materials
import com.zakgof.korender.declaration.Meshes
import com.zakgof.korender.declaration.Textures
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.z

@Composable
fun WaterExample() {

    Korender {

        val freeCamera = FreeCamera(Vec3(0f, 5f, 30f), -1.z)
        OnTouch { freeCamera.touch(it) }

        Frame {
            Camera(freeCamera.camera(projection, width, height, 0f))
            Renderable(
                mesh = Meshes.cube(2f),
                material = Materials.standard {
                    colorTexture = Textures.texture("/sand.jpg")
                }
            )
            Filter("effect/water.frag")
            Sky("cloud")
            Gui {
                Filler()
                Text(id = "fps", fontResource = "/ubuntu.ttf", height = 50, text = "FPS ${frameInfo.avgFps}", color = Color(0x66FF55))
            }
        }
    }
}