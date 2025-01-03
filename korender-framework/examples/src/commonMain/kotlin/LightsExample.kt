package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.Color
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
        camera = camera(Vec3(0f, 3f, 20f), -1.z, 1.y)
        Frame {
            PointLight(2.y + 4.x * sin(frameInfo.time) + 4.z * cos(frameInfo.time), Color.Red)
            PointLight(2.y + 4.x * sin(frameInfo.time + 2f) + 4.z * cos(frameInfo.time + 2f), Color.Green)
            PointLight(2.y + 4.x * sin(frameInfo.time + 4f) + 4.z * cos(frameInfo.time + 4f), Color.Blue)
            AmbientLight(Color.white(0.05f))

            Renderable(
                standart {
                    baseColor = Color.White
                    pbr.metallic = 0.0f
                    pbr.roughness = 0.8f
                },
                mesh = cube(1f),
                transform = scale(20f, 1f, 20f)
            )

            Renderable(
                standart {
                    baseColor = Color.White
                    pbr.metallic = 0.0f
                    pbr.roughness = 0.4f
                },
                mesh = sphere(1f),
                transform = translate(4.y)
            )
        }
    }