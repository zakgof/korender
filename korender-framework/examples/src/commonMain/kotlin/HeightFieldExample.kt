package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.declaration.Materials.standard
import com.zakgof.korender.declaration.Meshes.heightField
import com.zakgof.korender.declaration.Textures.texture
import com.zakgof.korender.math.FloatMath.sin
import com.zakgof.korender.math.Vec3

@Composable
fun HeightFieldExample() {
    val freeCamera = FreeCamera(Vec3(0f, 10f, 0f), Vec3(0f, -1f, -1f).normalize())
    Korender {
        Scene {
            OnTouch { freeCamera.touch(it) }
            Camera(freeCamera.camera(projection, width, height, frameInfo.dt))
            Renderable(
                mesh = heightField(id = "terrain", 128, 128, 0.2f) { x, y ->
                    0.5f * (sin(x * 0.2f) + sin(y * 0.2f))
                },
                material = standard {
                    colorTexture = texture("/sand.jpg")
                }
            )
        }
    }
}