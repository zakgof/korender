package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.y
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun IblExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    Frame {
        CaptureEnv(0) {
            AmbientLight(white(1f))
            Renderable(
                standart {
                    baseColorTexture = texture("texture/asphalt-albedo.jpg")
                    normalTexture = texture("texture/asphalt-normal.jpg")
                    pbr.metallic = 0.2f
                },
                mesh = cube(1f),
                transform = scale(1000f, 1f, 1000f).translate(-2.y)
            )
        }
    }
}