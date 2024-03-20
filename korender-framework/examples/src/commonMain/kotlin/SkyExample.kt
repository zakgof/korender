package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.projection.FrustumProjection

@Composable
fun SkyExample() = Korender {
    onResize = {
        projection =
            FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }
    Scene {
        val preset = when ((frameInfo.time * 0.3).toInt() % 3) {
            0 -> "star"
            1 -> "cloud"
            else -> "fastcloud"
        }
        Sky(preset = preset)
    }
}