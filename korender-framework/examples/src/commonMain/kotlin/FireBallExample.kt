package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.declaration.Materials.standard
import com.zakgof.korender.declaration.Meshes
import com.zakgof.korender.declaration.Textures
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import kotlin.math.floor

@Composable
fun FireBallExample() = Korender {
    Scene {
        val phase = fract(frameInfo.time)
        Camera(DefaultCamera(Vec3(0f, 2f, 20f), -1.z, 1.y))
        Billboard (
            fragment = "effect/fireball.frag",
            material = {
                xscale = 8f * phase
                yscale = 8f * phase
                static("power", phase)
            },
            transparent = true
        )
        Renderable(
            mesh = Meshes.cube(1f),
            material = standard {
                colorTexture = Textures.texture("/sand.jpg")
            },
            transform = Transform().scale(9f).translate(-9.y)
        )
    }
}

fun fract(time: Float): Float = time - floor(time)
