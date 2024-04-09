package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.math.FloatMath.sin
import com.zakgof.korender.math.Vec3

@Composable
fun SmokeExample() = Korender {
    Scene {
        for (i in 1..20) {
            val phase = fract(frameInfo.time * 0.5f + 20f / i)
            Billboard(
                fragment = "effect/smoke.frag",
                material = {
                    xscale = 5f * phase + 0.5f
                    yscale = 5f * phase + 0.5f
                    dynamic("seed") { i / 20f }
                    dynamic("density") { 1.0f - phase }
                },
                position = Vec3(
                    2f * phase * sin(phase * 0.4f),
                    phase * phase * 8f + phase * 2f - 4f,
                    i * 0.01f
                )
            )
        }
    }
}

