package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.FrustumProjectionDeclaration
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
        val orbitCamera = OrbitCamera(this, Vec3(0f, 4f, 40f), Vec3(0f, 4f, 0f))
        OnTouch { orbitCamera.touch(it) }
        Frame {
            camera = orbitCamera.camera(projection, width, height)
            DirectionalLight(Vec3(1f, -1f, 0f).normalize(), white(5.0f)) {
                Cascade(mapSize = 1024, (projection as FrustumProjectionDeclaration).near, 30f)
                Cascade(mapSize = 1024, 25f, 100f)
            }
            AmbientLight(white(0.25f))

            Renderable(
                materialModifier,
                mesh = cube(1f),
                transform = scale(1000f, 1f, 1000f).translate(-0.5f.y)
            )
            for (i in -100..10) {
                Renderable(
                    materialModifier,
                    mesh = sphere(0.5f),
                    transform = translate(Vec3(-3f, 3f, i * 2f)),
                )
            }

            Renderable(
                materialModifier,
                mesh = cube(),
                transform = scale(1f, 1f, 50f).translate(Vec3(-5f, 3f, 0f))
            )

            Renderable(
                materialModifier,
                mesh = sphere(0.4f),
                transform = translate(Vec3(30f, 1f, 0f)),
            )

            val fp = projection as FrustumProjectionDeclaration
            Renderable(
                standart { baseColor = Red },
                mesh = sphere (0.1f),
                transform = translate((40 - 25).z + 0.5f.y),
            )
            Renderable(
                standart { baseColor = Red },
                mesh = sphere (0.1f),
                transform = translate((40 - 30).z + 0.5f.y),
            )

            // Filter(fragment("!shader/effect/shadow-debug.frag"))
        }
    }