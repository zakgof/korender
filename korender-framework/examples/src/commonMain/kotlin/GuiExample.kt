package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.math.Color
import com.zakgof.korender.projection.FrustumProjection

@Composable
fun GuiExample() = Korender {

    onResize = {
        projection =
            FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    Scene {
        Gui {
            Row {
                Filler()
                Image(imageResource = "/korender32.png", width = 100, height = 100)
                Text(id = "title", fontResource = "/ubuntu.ttf", height = 100, text = "Korender Demo", color = Color(0x803456))
                Filler()
            }
            Filler()
            Text(id = "fps", fontResource = "/ubuntu.ttf", height = 50, text = "FPS ${frameInfo.avgFps}", color = Color(0x66FF55))
        }
    }
}