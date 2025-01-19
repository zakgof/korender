package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.OrbitCamera
import com.zakgof.korender.math.Color.Companion.Red
import com.zakgof.korender.math.Color.Companion.white
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun CSMExample() =
    Korender(appResourceLoader = { Res.readBytes(it) }) {
        val materialModifier = standart {
            baseColorTexture = texture("texture/asphalt-albedo.jpg")
            triplanarScale = 1.0f
            pbr.metallic = 0.0f
            pbr.roughness = 0.9f
            pcss = false
        }
        val orbitCamera = OrbitCamera(this, Vec3(0f, 2f, 5f), Vec3(0f, 2f, 0f))
        OnTouch { orbitCamera.touch(it) }
        Frame {
            projection = frustum(4f * width / height, 4f, 4f, 10000f)
            camera = orbitCamera.camera(projection, width, height)
            DirectionalLight(Vec3(1f, -1f, 0f).normalize(), white(5.0f)) {
                Cascade(mapSize = 1024, 4f, 12f)
                Cascade(mapSize = 1024, 10f, 30f)
                Cascade(mapSize = 1024, 25f, 100f)
            }
            AmbientLight(white(0.25f))

            Renderable(
                materialModifier,
                mesh = cube(1f),
                transform = scale(1000f, 1f, 1000f).translate(-1.y)
            )
            for (i in 0..100) {
                Renderable(
                    materialModifier,
                    mesh = sphere(0.5f),
                    transform = translate(Vec3(-3f, 3f, -i * 2f)),
                )
            }

            Renderable(
                materialModifier,
                mesh = cube(),
                transform = scale(1f, 1f, 200f).translate(Vec3(-5f, 3f, -100f))
            )
            Renderable(
                standart { baseColor = Red },
                mesh = sphere(0.1f),
                transform = translate((-10 + 5).z),
            )
            Renderable(
                standart { baseColor = Red },
                mesh = sphere(0.1f),
                transform = translate((-12 + 5).z),
            )
            Renderable(
                standart { baseColor = Red },
                mesh = sphere(0.1f),
                transform = translate((-25 + 5).z),
            )
            Renderable(
                standart { baseColor = Red },
                mesh = sphere(0.1f),
                transform = translate((-30 + 5).z),
            )

            //  Filter(fragment("!shader/effect/shadow-debug.frag"))
        }
    }