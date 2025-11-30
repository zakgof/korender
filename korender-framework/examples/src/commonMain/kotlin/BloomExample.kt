package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.ColorRGBA.Companion.white
import com.zakgof.korender.math.Transform.Companion.rotate
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

@Composable
fun BloomExample() =
    Korender(appResourceLoader = { Res.readBytes(it) }) {

        Frame {
            camera = camera(20.z, -1.z, 1.y)
            DirectionalLight(Vec3(1f, -1f, -1f), ColorRGB.white(3f))
            DeferredShading {
                PostShading(bloomWide(amount = 2.0f, downsample = 2, highResolutionRatio = 0.1f))
            }
            Renderable(
                base(color = white(0.1f)),
                emission(ColorRGB(2.0f, 0.1f, 1.8f)),
                mesh = cylinderSide(7f, 0.16f),
                transform = rotate(1.z, 0.4f).translate(-2.5f.y + 1.x + 0.3f.z)
            )
            Renderable(
                base(color = white(0.1f)),
                emission(ColorRGB(0.1f, 0.8f, 0.8f)),
                mesh = cylinderSide(7f, 0.16f),
                transform = rotate(1.z, -0.3f).translate(-2.5f.y)
            )
            Renderable(
                base(color = ColorRGBA(0.5f, 0.9f, 0.3f, 1.0f)),
                mesh = sphere(1f),
                transform = translate(3.x)
            )
            Gui {
                Column {
                    Filler()
                    Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
                }
            }
        }
    }