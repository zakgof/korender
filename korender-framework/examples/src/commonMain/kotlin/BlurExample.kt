package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.x
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.sin

@OptIn(ExperimentalResourceApi::class)
@Composable
fun BlurExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    Frame {
        Pass {
            Renderable(
                standart {
                    baseColorTexture = texture("texture/asphalt-albedo.jpg")
                },
                mesh = sphere(3f),
                transform = translate(-2.x)
            )
        }
        val radius = 1.0f + sin(frameInfo.time)
        Pass {
            Screen(blurHorz {
                this.radius = radius
            })
        }
        Pass {
            Screen(blurVert {
                this.radius = radius
            })
            Renderable(
                standart {
                    baseColorTexture = texture("texture/asphalt-albedo.jpg")
                },
                mesh = sphere(3f),
                transform = translate(2.x)
            )
        }
    }
}