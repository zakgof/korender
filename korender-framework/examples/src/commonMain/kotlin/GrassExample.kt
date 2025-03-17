package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun GrassExample() =
    Korender(appResourceLoader = { Res.readBytes(it) }) {

        val cam = FreeCamera(this, Vec3(0f, 5f, 0f), -1.z)
        val grass = grass("grass", 6) {
            it.x * it.x + it.z * it.z < 25 * 25f
        }

        OnKey { cam.handle(it) }
        OnTouch { cam.touch(it) }

        Frame {

            projection = frustum(3f * width / height, 3f, 3f, 1000f)
            camera = cam.camera(projection, width, height, frameInfo.dt)

            AmbientLight(ColorRGB.white(0.3f))
            DirectionalLight(Vec3(0.0f, -1.0f, 0.0f), ColorRGB.white(1.0f))

            Renderable(
                standart {
                    baseColorTexture = texture("texture/grass.jpg")
                    triplanarScale = 0.04f
                    pbr.metallic = 0f
                    pbr.roughness = 0.9f
                },
                mesh = cube(1f),
                transform = scale(30f, 1f, 30f).translate(-1.y)
            )

            Renderable(prefab = grass)

            Sky(fastCloudSky())

            Gui {
                Column {
                    Filler()
                    Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
                }
            }
        }
    }