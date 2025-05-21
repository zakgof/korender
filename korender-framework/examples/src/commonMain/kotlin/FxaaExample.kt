package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGB.Companion.White
import com.zakgof.korender.math.ColorRGBA.Companion.Green
import com.zakgof.korender.math.ColorRGBA.Companion.Red
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun FxaaExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    Frame {
        AmbientLight(White)
        Renderable(
            base(color = Green),
            mesh = sphere(4f),
        )
        val doFxaa = (frameInfo.time.toInt() % 6 < 3)
        Gui {
            val text = if (doFxaa) "FXAA ON" else "FXAA OFF"
            Text(text = text, fontResource = "font/orbitron.ttf", height = 50, color = Red, id = "fxaa")
        }

        if (doFxaa) {
            PostProcess(fxaa())
        }
    }
}