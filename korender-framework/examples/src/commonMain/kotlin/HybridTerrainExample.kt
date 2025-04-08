package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun HybridTerrainExample() =
    Korender(appResourceLoader = { Res.readBytes(it) }) {

        val terrain = clipmapTerrainPrefab("terrain", 2.0f, 16, 12)
        val roiTextures = roiTextures {
            RoiTexture(0.09f, 0.82f, 0.08f, texture("hybridterrain/runway.png"))
        }

        Frame {
            projection = frustum(5f, 5f * height / width, 2f, 32000f)

            // camera = camera(Vec3(-6100.0f, 600f, 6000.0f), (-1).y, 1.z)
            // camera = camera(Vec3(0.0f, 8000f, 0.0f), -1.y,  1.z)

            camera = camera(Vec3(23.0f, 1000f, -2000.0f), 1.z - 1.y, 1.z + 1.y)



            AmbientLight(ColorRGB.white(0.2f))
            DirectionalLight(Vec3(1.0f, -1.0f, 0.0f), ColorRGB.white(0.5f))
            Renderable(
                standart {
                    pbr.metallic = 0.0f
                },
                terrain {
                    heightTextureSize = 8192
                    heightTexture = texture("hybridterrain/base-height.jpg")
                    set("sdf", texture("hybridterrain/sdf.png", TextureFilter.Linear))
                    set("road", texture("infcity/road.jpg"))
                },
                roiTextures,
                plugin("terrain", "hybridterrain/height.glsl"),
                plugin("albedo", "hybridterrain/albedo.glsl"),

                prefab = terrain
            )
            Sky(fastCloudSky())
            PostProcess(water(), fastCloudSky())
            PostProcess(fog {
                color = ColorRGB(0x808090)
                density = 0.00003f
            })
            Gui {
                Column {
                    Filler()
                    Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
                }
            }
        }
    }