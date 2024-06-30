package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.math.Color
import com.zakgof.korender.onClick
import com.zakgof.korender.projection.FrustumProjection

@Composable
fun GuiExample() = Korender {
    var clicked = false
    Frame {
        Projection(FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f))
        Gui {
            Row {
                Filler()
                Image(imageResource = "/korender32.png", width = 100, height = 100)
                Text(id = "title", font = "/ubuntu.ttf", height = 100, text = "Korender Demo", color = Color(0xFF803456))
                Filler()
            }
            Row {
                Filler()
                if (clicked) {
                    Text(id = "thanks", font = "/ubuntu.ttf", height = 30, text = "Thank you for clicking", color = Color(0xFF888888), onTouch = {
                        onClick(it) { clicked = false }
                    })
                } else {
                    Text(id = "clicker", font = "/ubuntu.ttf", height = 50, text = "CLICK ME", color = Color(0xFF8044FF), onTouch = {
                        onClick(it) { clicked = true }
                    })
                }
                Filler()
            }
            Filler()
            Text(id = "fps", font = "/ubuntu.ttf", height = 50, text = "FPS ${frameInfo.avgFps}", color = Color(0xFF66FF55))
        }
    }
}