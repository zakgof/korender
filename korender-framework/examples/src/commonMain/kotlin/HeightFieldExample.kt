package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.material.MaterialModifiers.standart
import com.zakgof.korender.material.Textures.texture
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.mesh.Meshes.heightField
import kotlin.math.sin

@Composable
fun HeightFieldExample() {
    val freeCamera = FreeCamera(10.y, Vec3(0f, -1f, -1f).normalize())
    Korender {
        Frame {
            OnTouch { freeCamera.touch(it) }
            Camera(freeCamera.camera(projection, width, height, frameInfo.dt))
            Renderable(
                standart {
                    colorTexture = texture("sand.jpg")
                },
                mesh = heightField(id = "terrain", 128, 128, 0.2f) { x, y ->
                    0.5f * (sin(x * 0.2f) + sin(y * 0.2f))
                }
            )
        }
    }
}