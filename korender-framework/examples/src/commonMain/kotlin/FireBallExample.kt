package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.material.Effects.FireBall
import com.zakgof.korender.material.MaterialModifiers.effect
import com.zakgof.korender.material.MaterialModifiers.standart
import com.zakgof.korender.material.Textures.texture
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import com.zakgof.korender.mesh.Meshes.cube
import kotlin.math.floor

@Composable
fun FireBallExample() = Korender {
    Frame {
        val phase = fract(frameInfo.time)
        Camera(DefaultCamera(Vec3(0f, 2f, 20f), -1.z, 1.y))
        Billboard(
            effect(FireBall) {
                xscale = 8f * phase
                yscale = 8f * phase
                power = phase
            },
            transparent = true
        )
        Renderable(
            standart {
                colorTexture = texture("/sand.jpg")
            },
            mesh = cube(1f),
            transform = scale(9f).translate(-9.y)
        )
    }
}

fun fract(time: Float): Float = time - floor(time)
