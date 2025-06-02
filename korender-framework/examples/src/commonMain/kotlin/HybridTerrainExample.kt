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
import kotlin.random.Random

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
            DirectionalLight(Vec3(1.0f, -1.0f, 0.0f), ColorRGB.white(1.5f)) {
                // Cascade(1024, 5f, 2000f, algorithm = vsm())
            }
            Renderable(
                base(metallicFactor = 0.0f),
                roiTextures,
                plugin("normal", "!shader/plugin/normal.terrain.frag"),
                plugin("terrain", "hybridterrain/height.glsl"),
                plugin("albedo", "hybridterrain/albedo.glsl"),
                uniforms(
                    "heightTexture" to texture("hybridterrain/base-height.jpg"),
                    "sdf" to texture("hybridterrain/sdf.png", TextureFilter.Linear),
                    "road" to texture("infcity/road.jpg")
                ),
                prefab = terrain
            )

            fun cubTex(prefix: String) = cubeTexture(CubeTextureSide.entries.associateWith { "hybridterrain/tree/$prefix-${it.toString().lowercase()}.jpg" })

            Billboard(
                billboard(xscale = 100.0f, yscale = 100.0f),
                base(metallicFactor = 0f, roughnessFactor = 0.9f),
                radiant(
                    radiantTexture = cubTex("radiant"),
                    radiantNormalTexture = cubTex("radiant-normal"),
                    colorTexture = cubTex("albedo"),
                    normalTexture = cubTex("normal")
                ),
                instancing = billboardInstancing(
                    id = "trees",
                    dynamic = false,
                    count = 2000
                ) {
                    (0 until 2000).forEach {
                        val r = Random(it)
                        Instance(pos = Vec3(r.nextFloat() * 5000f - 600f, 450f, r.nextFloat() * 3000f - 1610f))
                    }
                })
            Sky(fastCloudSky())
            PostProcess(water(), fastCloudSky())
            PostProcess(fog(color = ColorRGB(0x9BB4C8), density = 0.00003f))
            Gui {
                Column {
                    Filler()
                    Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
                }
            }
        }
    }