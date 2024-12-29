package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.Color.Companion.Green
import com.zakgof.korender.math.Color.Companion.Red
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun FxaaExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    Frame {
        Pass {
            Renderable(
                standart {
                    baseColor = Green
                },
                mesh = sphere(4f),
            )
        }
        if (frameInfo.time.toInt() % 6 < 3) {
            Pass {
                Screen(fxaa())
                Gui {
                    Text(text = "FXAA", fontResource = "font/orbitron.ttf", height = 50, color = Red, id = "fxaa")
                }
            }
        }
    }
}