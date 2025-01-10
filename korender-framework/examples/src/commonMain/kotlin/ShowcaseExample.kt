package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.Color.Companion.Green
import com.zakgof.korender.math.Color.Companion.Red
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
            Text(text = "FPS ${frameInfo.avgFps}", height = 50, color = Red, fontResource = "font/orbitron.ttf", id = "fps")
        }
        Billboard(fire { yscale = 10f; xscale = 2f }, position = 6.y, transparent = true)
        Filter(water(), fastCloudSky())
    }
}