package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.OrbitCamera
import com.zakgof.korender.material.MaterialModifiers.standart
import com.zakgof.korender.material.Textures.texture
import com.zakgof.korender.math.FloatMath
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import com.zakgof.korender.mesh.Meshes.obj

@Composable
fun ObjFileExample() {
    val orbitCamera = OrbitCamera(20.z, 0.z)
    Korender {
        OnTouch { orbitCamera.touch(it) }
        Frame {
            Camera(orbitCamera.camera(projection, width, height))
            Renderable(
                standart {
                    colorTexture = texture("/head.jpg")
                },
                mesh = obj("/head.obj"),
                transform = scale(7.0f).rotate(1.y, -FloatMath.PIdiv2)
            )
        }
    }

}