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

@Composable
fun ProcTerrainExample() =
    Korender(appResourceLoader = { Res.readBytes(it) }) {

        val terrain = clipmapTerrainPrefab("terrain", 2.0f, 16, 13)
        Frame {

            projection = frustum(5f, 5f * height / width, 2f, 40000f)
            camera = camera(
                Vec3(frameInfo.time * 200.0f, 1500f + 100f * sin(frameInfo.time), frameInfo.time * 600.0f),
                2.x + 3.z,
                1.y
            )

            AmbientLight(ColorRGB.white(0.2f))
            DirectionalLight(Vec3(1.0f, -1.0f, 0.0f), ColorRGB.White)
            Renderable(
                base(metallicFactor = 0.0f),
                plugin("normal", "!shader/plugin/normal.terrain.frag"),
                plugin("terrain", "procterrain/height.glsl"),
                plugin("albedo", "procterrain/albedo.glsl"),
                prefab = terrain
            )
            PostProcess(fog(density = 0.00006f, color = ColorRGB(0xB8CAE9))) {
                Sky(fastCloudSky())
            }
            Gui {
                Column {
                    Filler()
                    Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
                }
            }
        }
    }