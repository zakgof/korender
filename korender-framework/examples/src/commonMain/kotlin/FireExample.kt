package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.material.Effects.Fire
import com.zakgof.korender.material.MaterialModifiers.effect
import kotlin.math.sin

@Composable
fun FireExample() = Korender {
    Frame {
        Billboard(effect(Fire) {
            xscale = 2.0f
            yscale = 10.0f
            strength = 2.0f + 2.0f * sin(frameInfo.dt * 0.5f)
        })
    }
}