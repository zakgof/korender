package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.material.Effects.BlurHorz
import com.zakgof.korender.material.Effects.BlurVert
import com.zakgof.korender.material.MaterialModifiers.effect
import com.zakgof.korender.material.MaterialModifiers.standart
import com.zakgof.korender.material.StandartMaterialOption
import com.zakgof.korender.material.Textures.texture
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.x
import com.zakgof.korender.mesh.Meshes.sphere
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.sin

@OptIn(ExperimentalResourceApi::class)
@Composable
fun BlurExample() = Korender (appResourceLoader = { Res.readBytes(it) }) {
    Frame {
        Pass {
            Renderable(
                standart(StandartMaterialOption.NoLight) {
                    colorTexture = texture("!sand.jpg")
                },
                mesh = sphere(3f),
                transform = translate(-2.x)
            )
        }
        val radius = 1.0f + sin(frameInfo.time)
        Pass {
            Screen(effect(BlurHorz) {
                this.radius = radius
            })
        }
        Pass {
            Screen(effect(BlurVert) {
                this.radius = radius
            })
            Renderable(
                standart(StandartMaterialOption.NoLight) {
                    colorTexture = texture("sand.jpg")
                },
                mesh = sphere(3f),
                transform = translate(2.x)
            )
        }
    }
}