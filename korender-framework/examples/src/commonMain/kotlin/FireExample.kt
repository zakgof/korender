package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.material.MaterialModifiers.fragment
import com.zakgof.korender.material.MaterialModifiers.standardUniforms

@Composable
fun FireExample() = Korender {
    Frame {
        Billboard(
            fragment("effect/fire.frag"),
            standardUniforms {
                xscale = 2.0f
                yscale = 10.0f
                static("strength", 4.0f)
            })
    }
}