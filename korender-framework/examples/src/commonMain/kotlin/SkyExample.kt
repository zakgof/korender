package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.Skies
import com.zakgof.korender.projection.FrustumProjection

@Composable
fun SkyExample() = Korender {

    onResize = {
        projection =
            FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }
    // add(Skies.fastClouds(gpu))
    // add(Skies.fancyClouds(gpu))
    add(Skies.stars(gpu))
}