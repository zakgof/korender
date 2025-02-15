package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGB.Companion.White
import com.zakgof.korender.math.ColorRGBA
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.sin

@OptIn(ExperimentalResourceApi::class)
@Composable
fun BlurExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    Frame {
        AmbientLight(White)
        Renderable(
            standart {
                baseColorTexture = texture("texture/asphalt-albedo.jpg")
            },
            mesh = sphere(3f)
        )
        val radius = 5.0f + 5.0f * sin(frameInfo.time)
        PostProcess(blurHorz {
            this.radius = radius
        })
        PostProcess(blurVert {
            this.radius = radius
        })
        Gui {
            Column {
                Filler()
                Text(id = "fps", text = "BLUR ${radius.toInt()} | FPS ${frameInfo.avgFps.toInt()}", height = 40, color = ColorRGBA(0x66FF55A0))
            }
        }
    }
}
