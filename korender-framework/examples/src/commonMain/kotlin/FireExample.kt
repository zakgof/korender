package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.declaration.Materials.billboardStandard

@Composable
fun FireExample() = Korender {
    Frame {
        Billboard(billboardStandard("effect/fire.frag") {
            xscale = 2.0f
            yscale = 10.0f
            static("strength", 4.0f)
        })
    }
}