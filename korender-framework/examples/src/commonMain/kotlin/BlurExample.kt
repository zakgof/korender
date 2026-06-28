package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.Slider
import com.zakgof.korender.SliderState
import com.zakgof.korender.math.ColorRGB.Companion.White
import com.zakgof.korender.math.ColorRGBA

@Composable
fun BlurExample() = Korender(resourceLoader = { Res.readBytes("files/$it") }) {
    val slider = SliderState(7f, 0f, 50f)
    Frame {
        TestExchange.report(frameInfo)
        AmbientLight(White)
        Renderable(
            base { colorTexture = texture("texture/asphalt-albedo.jpg") },
            mesh = sphere(3f)
        )
        PostProcess(blur(slider.position.toInt().toFloat()))
        Gui {
            Column {
                Filler()
                Row {
                    Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}", height = 64f, color = ColorRGBA(0x66FF55A0))
                    Filler()
                    Text(id = "blur", height = 64f, text = "R=${slider.position.toInt()} ")
                    Slider(id = "blur-slider", width = width * 0.5f, height = 64f, state = slider)
                }
            }
        }
    }
}

