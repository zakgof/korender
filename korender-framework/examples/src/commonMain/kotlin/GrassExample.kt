package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Attributes.PHI
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.random.Random

@OptIn(ExperimentalResourceApi::class)
@Composable
fun GrassExample() =
    Korender(appResourceLoader = { Res.readBytes(it) }) {

        val cam = FreeCamera(this, Vec3(0f, 4f, 20f), -1.z)

        OnKey { cam.handle(it) }
        OnTouch { cam.touch(it) }

        Frame {

            camera = cam.camera(projection, width, height, frameInfo.dt)

            DirectionalLight(Vec3(1.0f, -1.0f, 1.0f), ColorRGB.white(2.5f))

            Renderable(
                standart {
                    baseColorTexture = texture("texture/asphalt-albedo.jpg")
                    normalTexture = texture("texture/asphalt-normal.jpg")
                    pbr.metallic = 0.2f
                },
                mesh = cube(1f),
                transform = scale(30f, 1f, 30f).translate(-1.y)
            )

            Renderable(
                vertex("!shader/effect/grass.vert"),
                defs("VCOLOR"),
                standart {
                    pbr.metallic = 0.0f
                },
                mesh = customMesh("grassblade", 9, 42, POS, TEX, PHI) {
                    pos(Vec3.ZERO).tex(0f, 0f)
                    pos(Vec3.ZERO).tex(1f, 0f)
                    pos(Vec3.ZERO).tex(0f, 0.25f)
                    pos(Vec3.ZERO).tex(1f, 0.25f)
                    pos(Vec3.ZERO).tex(0f, 0.50f)
                    pos(Vec3.ZERO).tex(1f, 0.50f)
                    pos(Vec3.ZERO).tex(0f, 0.75f)
                    pos(Vec3.ZERO).tex(1f, 0.75f)
                    pos(Vec3.ZERO).tex(0.5f, 1.0f)

                    index(0, 1, 2, 1, 3, 2)
                    index(2, 3, 4, 3, 5, 4)
                    index(4, 5, 6, 5, 7, 6)
                    index(6, 7, 8)

                    index(1, 0, 2, 3, 1, 2)
                    index(3, 2, 4, 5, 3, 4)
                    index(5, 4, 6, 7, 5, 6)
                    index(7, 6, 8)
                },
                instancing = positionInstancing("grass", 5000, true) {
                    val r = Random(0L)
                    for (i in 0 until 5000) {
                        Instance(translate(r.nextFloat() * 20f - 10f, 0f, r.nextFloat() * 40f - 20f))
                    }
                }
            )


            Sky(fastCloudSky())

            Gui {
                Column {
                    Filler()
                    Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
                }
            }
        }
    }