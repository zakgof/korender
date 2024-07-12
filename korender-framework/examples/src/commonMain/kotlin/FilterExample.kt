package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.material.Effects.Adjust
import com.zakgof.korender.material.MaterialModifiers.effect
import com.zakgof.korender.material.MaterialModifiers.standart
import com.zakgof.korender.material.StandartMaterialOption
import com.zakgof.korender.material.Textures.texture
import com.zakgof.korender.math.Color
import com.zakgof.korender.mesh.Meshes.sphere
import kotlin.math.sin

@Composable
fun FilterExample() = Korender {
    Frame {
        Pass {
            Renderable(
                standart(StandartMaterialOption.NoLight){
                    colorTexture = texture("/sand.jpg")
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
                Text(text = "SATURATION $value", id = "saturation", font = "/ubuntu.ttf", height = 50, color = Color.Red)
            }
        }
    }
}