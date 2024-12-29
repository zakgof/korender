package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.material.Effects.Adjust
import com.zakgof.korender.material.MaterialModifiers.effect
import com.zakgof.korender.material.MaterialModifiers.standart
import com.zakgof.korender.material.StandartMaterialOption
import com.zakgof.korender.math.Color
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.sin

@OptIn(ExperimentalResourceApi::class)
@Composable
fun FilterExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    Frame {
        Pass {
            Renderable(
                standart(StandartMaterialOption.AlbedoMap) {
                    albedoTexture = texture("sand.jpg")
                },
                mesh = sphere(4f),
            )
        }
        Pass {
            val value = 1f + sin(frameInfo.time)
            Screen(effect(Adjust) {
                saturation = value
            })
            Gui {
                Filler()
                Text(
                    text = "SATURATION $value",
                    id = "saturation",
                    fontResource = "font/orbitron.ttf",
                    height = 50,
                    color = Color.Red
                )
            }
        }
    }
}