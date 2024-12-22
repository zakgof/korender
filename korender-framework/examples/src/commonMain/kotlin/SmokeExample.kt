package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.material.Effects.Smoke
import com.zakgof.korender.material.MaterialModifiers.effect
import com.zakgof.korender.material.MaterialModifiers.sky
import com.zakgof.korender.material.Skies.FastCloud
import com.zakgof.korender.math.y
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SmokeExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    Frame {
        Sky(sky(FastCloud))
        val n = 100
        for (i in 1..n) {
            val phase = fract(frameInfo.time * 0.5f + n.toFloat() / i)
            Billboard(
                effect(Smoke) {
                    xscale = 5f * phase + 0.5f
                    yscale = 5f * phase + 0.5f
                    seed = i / n.toFloat()
                    density = 1.0f - phase
                },
                position = (phase * phase * 8f + phase * 2f - 4f).y,
                transparent = true
            )
        }

    }
}

