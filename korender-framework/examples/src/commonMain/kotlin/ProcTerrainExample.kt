package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ProcTerrainExample() =
    Korender(appResourceLoader = { Res.readBytes(it) }) {

        val terrain = clipmapTerrainPrefab("terrain", 2.0f, 16, 13)
        Frame {

            val cloudy = 0.5f - 0.5f * cos(frameInfo.time * 0.5f)

            projection = projection(5f, 5f * height / width, 2f, 40000f)
            camera = camera(
                Vec3(frameInfo.time * 200.0f, 1500f + 100f * sin(frameInfo.time), frameInfo.time * 600.0f),
                2.x + 3.z,
                1.y
            )

            AmbientLight(ColorRGB.white(0.3f - 0.3f * cloudy))
            DirectionalLight(Vec3(1.0f, -1.0f, 0.0f), ColorRGB.white(1.1f - cloudy))
            Renderable(
                base(metallicFactor = 0.0f),
                plugin("normal", "!shader/plugin/normal.terrain.frag"),
                plugin("terrain", "procterrain/height.glsl"),
                plugin("albedo", "procterrain/albedo.glsl"),
                prefab = terrain
            )

            val zenithRGB = ColorRGB(0.102f, 0.322f, 0.620f) * (1.0f - cloudy) + ColorRGB.white(0.300f) * cloudy
            val horizonRGB = zenithRGB * (0.3f + 0.7f * cloudy) + ColorRGB.white(0.7f * (1.0f - cloudy))
            val cloudDensity = 3f + cloudy * 2f
            val rippleAmount = 0.3f + cloudy * 0.2f
            val cloudLight = 1.0f - cloudy * 0.6f
            val cloudDark = 0.7f - cloudy * 0.5f
            val fogDensity = 2e-5f * (1.0f + 2.0f * cloudy)
            val fogColor = horizonRGB * (1.0f - cloudy) + ColorRGB.white(cloudDark) * cloudy

            PostProcess(fog(density = fogDensity, color = fogColor)) {
                Sky(fastCloudSky(zenithColor = zenithRGB, horizonColor = horizonRGB, density = cloudDensity, rippleAmount = rippleAmount, cloudLight = cloudLight, cloudDark = cloudDark))
            }
            PostProcess(fxaa())
            Gui {
                Column {
                    Filler()
                    Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
                }
            }
        }
    }