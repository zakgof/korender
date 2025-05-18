package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.CubeTextureSide
import com.zakgof.korender.Korender
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.random.Random

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
            DirectionalLight(Vec3(1.0f, -1.0f, 0.0f), ColorRGB.white(1.5f))
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

            fun cubTex(prefix: String) = cubeTexture(CubeTextureSide.entries.associateWith { "hybridterrain/tree/$prefix-${it.toString().lowercase()}.jpg" })

            InstancedBillboards(
                standart {
                    xscale = 100.0f
                    yscale = 100.0f
                    set("radiantTexture", cubTex("radiant"))
                    set("radiantNormalTexture", cubTex("radiant-normal"))
                    set("colorTexture", cubTex("albedo"))
                    set("normalTexture", cubTex("normal"))
                },
                fragment("!shader/effect/radial.frag"),
                id = "trees",
                static = true,
                count = 20000
            ) {
                (0 until 20000).forEach {
                    val r = Random(it)
                    Instance(pos = Vec3(r.nextFloat() * 5000f - 600f, 450f, r.nextFloat() * 3000f - 1610f))
                }
            }
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