package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.CubeTextureSide
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA.Companion.Red
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

@Composable
fun CSMExample() =
    Korender(appResourceLoader = { Res.readBytes(it) }) {
        val materialModifiers = arrayOf(
            base(colorTexture = texture("texture/asphalt-albedo.jpg"), metallicFactor = 0f, roughnessFactor = 0.9f),
            triplanar(scale = 1.0f)
        )
        val freeCamera = FreeCamera(this, Vec3(0f, 2f, 5f), (-1).z)
        OnTouch { freeCamera.touch(it) }
        OnKey { freeCamera.handle(it) }

        Frame {

            CaptureFrame("oak", 1024, 1024, camera(50.z, -1.z, 1.y), ortho(50f, 50f, 1f, 100f)) {
                AmbientLight(white(1.6f))
                Gltf(
                    resource = "gltf/ai/swat.glb",
                    transform = scale(0.1f)
                )
                Renderable(
                    base(color = Red),
                    mesh = sphere(10f)
                )
            }

            projection = frustum(4f * width / height, 4f, 4f, 10000f)
            camera = freeCamera.camera(projection, width, height, frameInfo.dt)
            DirectionalLight(Vec3(1f, -1f, 0.3f).normalize(), white(5.0f)) {
                Cascade(1024, 4f, 12f, -0f to 50f, pcss())
                Cascade(1024, 10f, 30f, 0f to 50f, vsm())
                Cascade(1024, 25f, 100f, 0f to 50f, vsm())
            }
            Renderable(
                *materialModifiers,
                mesh = cube(1f),
                transform = scale(1000f, 1f, 1000f).translate(-1.y)
            )
            for (i in 0..100) {
                Renderable(
                    *materialModifiers,
                    mesh = sphere(0.5f),
                    transform = translate(Vec3(-3f, 3f, -i * 2f)),
                )
            }

            Renderable(
                *materialModifiers,
                mesh = cube(),
                transform = scale(1f, 1f, 200f).translate(Vec3(-5f, 3f, -100f))
            )
            Renderable(
                *materialModifiers,
                mesh = cube(),
                transform = scale(2f, 10f, 2f).translate(Vec3(-7f, 5f, -10f))
            )
            Renderable(
                base(color = Red),
                mesh = sphere(0.1f),
                transform = translate((-10 + 5).z),
            )
            Renderable(
                base(color = Red),
                mesh = sphere(0.1f),
                transform = translate((-12 + 5).z),
            )
            Renderable(
                base(color = Red),
                mesh = sphere(0.1f),
                transform = translate((-25 + 5).z),
            )
            Renderable(
                base(color = Red),
                mesh = sphere(0.1f),
                transform = translate((-30 + 5).z),
            )
            Renderable(
                base(color = Red),
                mesh = sphere(0.5f),
                transform = translate(2f, 0.5f, -3f),
            )
            fun cubTex(prefix: String) = cubeTexture(CubeTextureSide.entries.associateWith { "hybridterrain/tree/$prefix-${it.toString().lowercase()}.jpg" })

            Billboard(
                billboard(xscale = 5.0f, yscale = 5.0f),
                base(metallicFactor = 0f, roughnessFactor = 0.9f),
                radiant(
                    radiantTexture = cubTex("radiant"),
                    radiantNormalTexture = cubTex("radiant-normal"),
                    colorTexture = cubTex("albedo"),
                    normalTexture = cubTex("normal")
                ),
                position = Vec3(5f, 2f, -10f)
            )

//            Billboard(
//                billboard(xscale = 5.0f, yscale = 5.0f),
//                base(
//                    colorTexture = textureProbe("oak"),
//                    metallicFactor = 0f,
//                    roughnessFactor = 0.9f
//                ),
//                position = Vec3(5f, 2f, -10f)
//            )

            Gui {
                Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
            }
            // Filter(fragment("!shader/effect/shadow-debug.frag"))
        }
    }