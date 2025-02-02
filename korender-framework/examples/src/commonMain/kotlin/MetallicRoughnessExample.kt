package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MetallicRoughnessExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    Frame {
        background = ColorRGB(0.1f, 0.1f, 0.15f)
        DirectionalLight(Vec3(1.0f, -1.0f, 0.0f), ColorRGB.white(3f))
        DirectionalLight(Vec3(-1.0f, 1.0f, 0.0f), ColorRGB.white(0.2f))
        AmbientLight(ColorRGB.Black)
        for (m in 0..4) {
            for (r in 0..4) {
                Renderable(
                    standart {
                        baseColor = ColorRGBA.White
                        pbr.metallic = (r / 4.0f)
                        pbr.roughness = (m / 4.0f)
                    },
                    mesh = sphere(0.8f),
                    transform = translate((m - 2) * 1.7f, (r - 2) * 1.7f, 0f)
                )
            }
        }
        Gui {
            Column {
                Filler()
                Text(id = "fps", fontResource = "font/orbitron.ttf", height = 30, text = "FPS ${frameInfo.avgFps.toInt()}", color = ColorRGBA(0x66FF55B0))
            }
        }
    }
}