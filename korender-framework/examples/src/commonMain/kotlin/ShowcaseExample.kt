package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.material.Effects.Fire
import com.zakgof.korender.material.Effects.Water
import com.zakgof.korender.material.MaterialModifiers.effect
import com.zakgof.korender.material.MaterialModifiers.sky
import com.zakgof.korender.material.MaterialModifiers.standart
import com.zakgof.korender.material.Skies.FastCloud
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
    Frame {
        Camera(DefaultCamera(Vec3(0f, 5f, 30f), -1.z, 1.y))
        Pass {
            Sky(sky(FastCloud))
            Renderable(standart { baseColor = Green }, mesh = sphere(2f), transform = translate(-0.5f.y))
        }
        Pass {
            Screen(effect(Water), sky(FastCloud))
            Billboard(effect(Fire) { yscale = 10f; xscale = 2f }, position = 6.y, transparent = true)
            Gui {
                Filler()
                Text(text = "FPS ${frameInfo.avgFps}", height = 50,  color = Red, fontResource = "font/orbitron.ttf", id = "fps")
            }
        }
    }
}