package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.declaration.MaterialModifiers.fragment
import com.zakgof.korender.declaration.MaterialModifiers.plugin
import com.zakgof.korender.declaration.MaterialModifiers.standardUniforms
import com.zakgof.korender.declaration.Meshes.cube
import com.zakgof.korender.declaration.Textures.texture
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
            val plugin = plugin("sky", "sky/fastcloud.plugin.frag")
            Camera(freeCamera.camera(projection, width, height, 0f))
            Pass {
                Renderable(
                    standardUniforms {
                        colorTexture = texture("/sand.jpg")
                    },
                    mesh = cube(2f),
                )
                Sky(plugin)
            }
            Pass {
                Screen(fragment("effect/water.frag"), plugin)
                Gui {
                    Filler()
                    Text(id = "fps", fontResource = "/ubuntu.ttf", height = 50, text = "FPS ${frameInfo.avgFps}", color = Color(0xFFFF0000))
                }
            }
        }
    }
}