package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.camera.DefaultCamera
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
            albedoTexture = texture("sand.jpg")
            pcss = true
        }
        Frame {
            Light(Vec3(1f, -1f, 1f).normalize())
            Camera(DefaultCamera(Vec3(-2.0f, 3f, 20f), -1.z, 1.y))
            Shadow {
                Cascade(mapSize = 1024, near = 10.0f, far = 25.0f)
            }
            Renderable(
                materialModifier,
                mesh = cube(1f),
                transform = scale(8f, 1f, 8f)
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
        }
    }