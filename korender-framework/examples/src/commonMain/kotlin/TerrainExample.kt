package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.cos

@OptIn(ExperimentalResourceApi::class)
@Composable
fun TerrainExample() =
    Korender(appResourceLoader = { Res.readBytes(it) }) {

        val terrain = clipmapTerrainPrefab("terrain", 2.0f, 10, 6)
        Frame {

            projection = frustum(5f, 5f * height / width, 2f, 9000f)
            camera = camera(Vec3(0f, 240f, -500f-800f * cos(frameInfo.time * 0.1f)), 1.y + 2.z, 2.y + 1.z)

            AmbientLight(ColorRGB.white(0.2f))
            DirectionalLight(Vec3(1.0f, -1.0f, 0.0f), ColorRGB.white(0.5f))

            Renderable(standart {
                pbr.metallic = 0.0f
                baseColorTexture = texture("terrain/terrain-albedo.jpg", wrap = TextureWrap.ClampToEdge)
            }, terrain {
                heightTexture = texture("terrain/terrain-height.png")
                heightTextureSize = 1024
                heightScale = 200.0f
                outsideHeight = -100.0f
                terrainCenter = Vec3(0f, -14f, 0f);
            }, prefab = terrain)

            Sky(fastCloudSky())
            PostProcess(water {
                waveScale = 1000.0f
            }, fastCloudSky())
            Gui {
                Column {
                    Filler()
                    Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
                }
            }
        }
    }