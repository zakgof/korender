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
import kotlin.math.round

@OptIn(ExperimentalResourceApi::class)
@Composable
fun GrassExample() =
    Korender(appResourceLoader = { Res.readBytes(it) }) {

        val cam = FreeCamera(this, Vec3(0f, 5f, 0f), -1.z)
        val grass = grassPrefab("grass", 4, 0.4f, 200) {
            val xsnap = round(it.x / 20f) * 20f
            val zsnap = round(it.z / 20f) * 20f
            (it.x - xsnap) * (it.x - xsnap) + (it.z - zsnap) * (it.z - zsnap) < 9*9
        }

        OnKey { cam.handle(it) }
        OnTouch { cam.touch(it) }

        Frame {

            projection = frustum(3f * width / height, 3f, 3f, 1000f)
            camera = cam.camera(projection, width, height, frameInfo.dt)

            AmbientLight(ColorRGB.white(0.3f))
            DirectionalLight(Vec3(0.0f, -1.0f, 0.0f), ColorRGB.white(1.0f))

            Renderable(
                base(colorTexture = texture("texture/grass.jpg"), metallicFactor = 0f, roughnessFactor = 0.9f),
                triplanar(0.04f),
                mesh = cube(1f),
                transform = scale(300f, 1f, 300f).translate(-1.y)
            )

            Renderable(
                grass(),
                prefab = grass
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