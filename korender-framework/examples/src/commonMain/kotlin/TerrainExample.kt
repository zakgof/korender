package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun TerrainExample() =
    Korender(appResourceLoader = { Res.readBytes(it) }) {

        val terrain = clipmapTerrain("terrain", 2.0f, 3, 12)
        val proj = frustum(5f, 5f * height / width, 2f, 9000f)
        projection = proj
        Frame {

            camera = camera(Vec3(4000f, 1024f, 6000f - (35f + frameInfo.time * 0.4f) * 100f), (-1.y + 2.z).normalize(), (2.y + 1.z).normalize())

            AmbientLight(ColorRGB.white(0.2f))
            DirectionalLight(Vec3(1.0f, -1.0f, 0.0f).normalize(), ColorRGB.white(0.5f))

            Renderable(standart {
                pbr.metallic = 0.0f;
            }, prefab = terrain)

            Sky(fastCloudSky())
            PostProcess(water(), fastCloudSky())
//            PostProcess(fog {
//                density = 0.0001f
//                color = ColorRGB(0x8090FF)
//            })
            Gui {
                Column {
                    Filler()
                    Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
                }
            }
        }
    }