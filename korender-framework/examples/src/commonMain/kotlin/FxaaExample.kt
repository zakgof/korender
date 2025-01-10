package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.Color.Companion.Green
import com.zakgof.korender.math.Color.Companion.Red
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun FxaaExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    Frame {
        Renderable(
            standart {
                baseColor = Green
                pbr.metallic = 0.2f
            },
            mesh = sphere(4f),
        )
        Gui {
            Text(text = "FXAA", fontResource = "font/orbitron.ttf", height = 50, color = Red, id = "fxaa")
        }
        if (frameInfo.time.toInt() % 6 < 3) {
            Filter(fxaa())
        }
    }
}