package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import kotlin.math.floor

@Composable
fun FireBallExample() = Korender {
    Scene {
        val phase = fract(frameInfo.time)
        Billboard (
            fragment = "effect/fireball.frag",
            material = {
                xscale = 8f * phase
                yscale = 8f * phase
                static("power", phase)
            }
        )
    }
}

fun fract(time: Float): Float = time - floor(time)
