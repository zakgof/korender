package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.material.MaterialModifiers.standardUniforms
import com.zakgof.korender.mesh.Meshes.cube
import com.zakgof.korender.material.Textures.texture
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.z

@Composable
fun InstancedMeshesExample() = Korender {
    val freeCamera = FreeCamera(20.z, -1.z)
    OnTouch { freeCamera.touch(it) }
    Frame {
        Camera(freeCamera.camera(projection, width, height, frameInfo.dt))
        InstancedRenderables(
            standardUniforms {
                colorTexture = texture("/sand.jpg")
            },
            id = "particles",
            count = 21 * 21,
            mesh = cube(0.4f),
            static = true
        ) {
            for (x in -10..10) {
                for (y in -10..10) {
                    Instance(transform = Transform().translate(Vec3(x.toFloat(), y.toFloat(), 0f)))
                }
            }
        }
        Gui {
            Row {
                Filler()
                Column {
                    Filler()
                    Image(imageResource = "/accelerate.png", width = 128, height = 128, onTouch = { freeCamera.forward(it) })
                    Image(imageResource = "/decelerate.png", width = 128, height = 128, onTouch = { freeCamera.backward(it) })
                }
            }
        }
    }
}