package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGB.Companion.White
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
        val radius = 1.0f + sin(frameInfo.time)
        Filter(blurHorz {
            this.radius = radius
        })
        Filter(blurVert {
            this.radius = radius
        })
    }
}
