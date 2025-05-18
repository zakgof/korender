package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.sin

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ProcTerrainExample() =
    Korender(appResourceLoader = { Res.readBytes(it) }) {

        val terrain = clipmapTerrainPrefab("terrain", 2.0f, 16, 12)
        Frame {

            projection = frustum(5f, 5f * height / width, 2f, 32000f)
            camera = camera(Vec3(frameInfo.time * 200.0f, 1500f + 100f * sin(frameInfo.time), frameInfo.time * 300.0f),
                2.x + 3.z,
                1.y)

            AmbientLight(ColorRGB.white(0.2f))
            DirectionalLight(Vec3(1.0f, -1.0f, 0.0f), ColorRGB.white(0.5f))
            Renderable(
                standart {
                    pbr.metallic = 0.0f
                },
                terrain {
                    heightTextureSize = 1024
                    terrainCenter = Vec3(0f, -14f, 0f);
                },
                plugin("terrain", "procterrain/height.glsl"),
                plugin("albedo", "procterrain/albedo.glsl"),

                prefab = terrain
            )
            Sky(fastCloudSky())
//            PostProcess(fog {
//                color = ColorRGB(0x8080F0)
//                density = 0.00003f
//            })
            Gui {
                Column {
                    Filler()
                    Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
                }
            }
        }
    }