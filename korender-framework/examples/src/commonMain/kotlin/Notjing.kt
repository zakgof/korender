package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGBA
import kotlin.math.sin

@Composable
fun Nothing(r: Float) {
    Korender(appResourceLoader = { Res.readBytes(it) }) {
        Frame {
            background = ColorRGBA(r, 0.5f + 0.5f * sin(frameInfo.time), 0.5f, 1f)
        }
    }
}