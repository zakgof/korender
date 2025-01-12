package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Color.Companion.Green
import com.zakgof.korender.math.Color.Companion.Red
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun FxaaExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    Frame {
        AmbientLight(Color.White)
        Renderable(
            standart {
                baseColor = Green
            },
            mesh = sphere(4f),
        )
        val doFxaa = (frameInfo.time.toInt() % 6 < 3)
        Gui {
            val text = if (doFxaa) "FXAA ON" else "FXAA OFF"
            Text(text = text, fontResource = "font/orbitron.ttf", height = 50, color = Red, id = "fxaa")
        }

        if (doFxaa) {
            Filter(fxaa())
        }
    }
}