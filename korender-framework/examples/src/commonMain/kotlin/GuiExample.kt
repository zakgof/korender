package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.Color
import com.zakgof.korender.onClick
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun GuiExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    var clicked = false
    Frame {
        Gui {
            Row {
                Filler()
                Image(imageResource = "korender32.png", width = 100, height = 100)
                Text(id = "title", fontResource = "font/anta.ttf", height = 100, text = "Korender Demo", color = Color(0xFF803456))
                Filler()
            }
            Row {
                Filler()
                if (clicked) {
                    Text(id = "thanks", fontResource = "font/orbitron.ttf", height = 30, text = "Thank you for clicking", color = Color(0xFF888888), onTouch = {
                        onClick(it) { clicked = false }
                    })
                } else {
                    Text(id = "clicker", fontResource = "font/orbitron.ttf", height = 50, text = "CLICK ME", color = Color(0xFF8044FF), onTouch = {
                        onClick(it) { clicked = true }
                    })
                }
                Filler()
            }
            Filler()
            Text(id = "fps", fontResource = "font/orbitron.ttf", height = 50, text = "FPS ${frameInfo.avgFps}", color = Color(0xFF66FF55))
        }
    }
}