package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGB.Companion.White
import com.zakgof.korender.math.ColorRGBA.Companion.Green
import com.zakgof.korender.math.ColorRGBA.Companion.Red

@Composable
fun FxaaExample() = Korender(resourceLoader = { Res.readBytes("files/$it") }) {
    Frame {
        TestExchange.report(frameInfo)
        AmbientLight(White)
        Renderable(
            base { color = Green },
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

