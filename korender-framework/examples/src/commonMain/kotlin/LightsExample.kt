package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalResourceApi::class)
@Composable
fun LightsExample() =
    Korender(appResourceLoader = { Res.readBytes(it) }) {
        camera = camera(Vec3(0f, 3f, 20f), (-1).z, 1.y)

        val mat = standart {
            baseColor = ColorRGBA.White
            pbr.metallic = 0.0f
            pbr.roughness = 0.7f
        }

        Frame {

            fun LightMark(pos: Vec3, color: ColorRGB) = Renderable (
                standart {
                    baseColor = ColorRGBA.Black
                    emissiveFactor = color
                },
                mesh = sphere(0.05f),
                transform = translate(pos)
            )

            val pos1 = 3.y + 4.x * sin(frameInfo.time + 0f) + 4.z * cos(frameInfo.time + 0f)
            val pos2 = 3.y + 4.x * sin(frameInfo.time + 2f) + 4.z * cos(frameInfo.time + 2f)
            val pos3 = 3.y + 4.x * sin(frameInfo.time + 4f) + 4.z * cos(frameInfo.time + 4f)

            AmbientLight(ColorRGB.white(0.1f))

            PointLight(pos1, ColorRGB.Red, 0.001f, 0.001f)
            PointLight(pos2, ColorRGB.Green, 0.001f, 0.001f)
            PointLight(pos3, ColorRGB.Blue, 0.001f, 0.001f)

            LightMark(pos1, ColorRGB.Red)
            LightMark(pos2, ColorRGB.Green)
            LightMark(pos3, ColorRGB.Blue)

            Renderable(
                mat,
                mesh = cube(1f),
                transform = scale(10f, 1f, 10f)
            )

            Renderable(
                mat,
                mesh = sphere(1f),
                transform = translate(4.y)
            )
            Gui {
                Filler()
                Text(id = "fps", fontResource = "font/orbitron.ttf", height = 30, text = "FPS ${frameInfo.avgFps.toInt()}", color = ColorRGBA(0x66FF55B0))
            }
        }
    }