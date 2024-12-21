package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.material.Effects.Fxaa
import com.zakgof.korender.material.MaterialModifiers.effect
import com.zakgof.korender.material.MaterialModifiers.standart
import com.zakgof.korender.material.StandartMaterialOption.FixedColor
import com.zakgof.korender.math.Color.Companion.Green
import com.zakgof.korender.math.Color.Companion.Red
import com.zakgof.korender.mesh.Meshes.sphere
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun FxaaExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    Frame {
        Pass {
            Renderable(
                standart(FixedColor) {
                    color = Green
                },
                mesh = sphere(4f),
            )
        }
        if (frameInfo.time.toInt() % 6 < 3) {
            Pass {
                Screen(effect(Fxaa))
                Gui {
                    Text(text = "FXAA", font = "!ubuntu.ttf", height = 50, color = Red, id = "fxaa")
                }
            }
        }
    }
}