package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.material.Effects.Fxaa
import com.zakgof.korender.material.MaterialModifiers.effect
import com.zakgof.korender.material.MaterialModifiers.standart
import com.zakgof.korender.material.StandartMaterialOption.FixedColor
import com.zakgof.korender.math.Color
import com.zakgof.korender.mesh.Meshes.sphere

@Composable
fun FxaaExample() = Korender {
    Frame {
        Pass {
            Renderable(
                standart(FixedColor) {
                    color = Color.Green
                },
                mesh = sphere(4f),
            )
        }
        if (frameInfo.time.toInt() % 6 < 3) {
            Pass {
                Screen(effect(Fxaa))
                Gui {
                    Text(text = "FXAA", id = "fxaa", font = "/ubuntu.ttf", height = 50, color = Color.Red)
                }
            }
        }
    }
}