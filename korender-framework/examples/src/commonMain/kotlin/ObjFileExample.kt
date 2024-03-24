package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.declaration.Materials.standard
import com.zakgof.korender.declaration.Meshes.obj
import com.zakgof.korender.material.Textures
import com.zakgof.korender.math.FloatMath
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x

@Composable
fun ObjFileExample() {
    val orbitCamera = OrbitCamera(Vec3(0f, 2f, 20f), Vec3(0f, 2f, 0f))
    Korender {
        OnTouch { orbitCamera.touch(it) }
        Scene {
            Camera(orbitCamera.camera(projection, width, height))
            Renderable(
                mesh = obj("/cat-red.obj"),
                material = standard {
                    colorTexture = Textures.texture("/cat-red.jpg")
                    ambient = 1.0f
                    diffuse = 1.0f
                    specular = 0.0f
                },
                transform = Transform().scale(0.1f).rotate(1.x, -FloatMath.PIdiv2)
            )
        }
    }

}