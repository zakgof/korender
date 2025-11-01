package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA.Companion.white
import com.zakgof.korender.math.Transform.Companion.rotate
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import kotlin.math.sin

@Composable
fun BloomExample() =
    Korender(appResourceLoader = { Res.readBytes(it) }) {

        Frame {
            camera = camera(20.z + 1.5f.x * sin(frameInfo.time * 0.4f), -1.z, 1.y)
            DeferredShading {
                // PostShading(bloom(radius = 36f))
                PostShading(bloomWide())
            }
            Renderable(
                base(color = white(0.5f)),
                emission(ColorRGB(1.0f, 0.1f, 0.8f)),
                mesh = cylinderSide(5f, 0.06f),
                transform = rotate(1.z, 0.4f).translate(-2.5f.y + 1.x + 0.3f.z)
            )
            Renderable(
                base(color = white(0.5f)),
                emission(ColorRGB(0.1f, 0.8f, 0.8f)),
                mesh = cylinderSide(5f, 0.06f),
                transform = rotate(1.z, -0.3f).translate(-2.5f.y)
            )
            Gui {
                Column {
                    Filler()
                    Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
                }
            }
        }
    }