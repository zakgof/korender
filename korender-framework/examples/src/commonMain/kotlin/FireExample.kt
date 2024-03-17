package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.projection.FrustumProjection

@Composable
fun FireExample() = Korender {

    onResize = {
        projection =
            FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

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