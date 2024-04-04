package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender

@Composable
fun FireExample() = Korender {
    Scene {
        Billboard (
            fragment = "fire.frag",
            material = {
                xscale = 2.0f
                yscale = 10.0f
                static("strength", 4.0f)
            }
        )
    }
}