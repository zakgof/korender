package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.ColorRGBA.Companion.Green
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ShowcaseExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    camera = camera(Vec3(0f, 5f, 30f), -1.z, 1.y)
    Frame {
        DirectionalLight(Vec3(1f, -1f, -1f))
        Sky(fastCloudSky())
        Renderable(
            standart {
                baseColor = Green
                pbr.metallic = 0.3f
                pbr.roughness = 0.5f
            },
            mesh = sphere(2f),
            transform = translate(-0.5f.y)
        )
        Gui {
            Filler()
            Text(id = "fps", fontResource = "font/orbitron.ttf", height = 30, text = "FPS ${frameInfo.avgFps.toInt()}", color = ColorRGBA(0x66FF55B0))
        }
        Billboard(fire { yscale = 10f; xscale = 2f }, position = 6.y, transparent = true)
        Filter(water(), fastCloudSky())
    }
}