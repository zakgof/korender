package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.sin

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ShadowExample() =
    Korender(appResourceLoader = { Res.readBytes(it) }) {
        val materialModifier = standart {
            baseColorTexture = texture("texture/asphalt-albedo.jpg")
            normalTexture = texture("texture/asphalt-normal.jpg")
            pbr.metallic = 0.2f
        }
        camera = camera(Vec3(-2.0f, 5f, 30f), -1.z, 1.y)
        Frame {
            DirectionalLight(Vec3(1f, -1f, 2f), white(5.0f)) {
                Cascade(mapSize = 1024, near = 10.0f, 40.0f, 0f to 10f, pcss())
            }
            DirectionalLight(Vec3(-1f, -1f, 2f), white(3.0f)) {
                Cascade(mapSize = 1024, near = 10.0f, 40.0f, 0f to 10f, vsm())
            }
            DirectionalLight(Vec3(0f, -1f, -3f), white(4.0f)) {
                Cascade(mapSize = 1024, near = 10.0f, 40.0f, 0f to 10f, vsm())
            }
            AmbientLight(white(0.05f))

            Renderable(
                materialModifier,
                mesh = cube(1f),
                transform = scale(10f, 1f, 10f)
            )
            Renderable(
                materialModifier,
                mesh = cube(1.0f),
                transform = translate(2.y).rotate(1.y, frameInfo.time * 0.1f),
            )
            Renderable(
                materialModifier,
                mesh = sphere(1.5f),
                transform = translate(Vec3(-5.0f, 3.5f + sin(frameInfo.time), 0.0f)),
            )
            Gui {
                Column {
                    Filler()
                    Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
                }
            }
        }
    }