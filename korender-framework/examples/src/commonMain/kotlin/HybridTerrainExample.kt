package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.CubeTextureSide
import com.zakgof.korender.Image
import com.zakgof.korender.Korender
import com.zakgof.korender.Prefab
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.math.roundToInt

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun HybridTerrainExample() =
    Korender(appResourceLoader = { Res.readBytes(it) }) {

        val terrain = clipmapTerrainPrefab("terrain", 32.0f, 24, 6)
        val heightMapLoading = loadImage("hybridterrain/height.png")
        val fbmLoading = loadImage("!fbm.png")

        Frame {
            projection = projection(5f, 5f * height / width, 2f, 32000f)
            camera = camera(Vec3(23.0f, 1000f, -2000.0f), 1.z - 1.y, 1.z + 1.y)

            AmbientLight(ColorRGB.white(0.2f))
            DirectionalLight(Vec3(1.0f, -1.0f, 0.0f), ColorRGB.white(1.5f))

            if (heightMapLoading.isCompleted && fbmLoading.isCompleted) {
                island(heightMapLoading.getCompleted(), fbmLoading.getCompleted(), terrain)
            }


            atmosphere()
            gui()
        }
    }

fun height(heightMap: Image, fbm: Image, x: Float, z: Float): Float {

    val u = 0.5f + x / (2.0f * 8192f)
    val v = 0.5f + z / (2.0f * 8192f)

    if (u < 0.0f || u > 1.0f || v < 0.0f || v > 1.0f)
        return -100f

    val sample = heightMap.pixel((u * 512).roundToInt(), (v * 512).roundToInt()).r // TODO interpolate

    return sample * 800f - 90f
}

private fun FrameContext.island(heightMap: Image, fbm: Image, terrain: Prefab) {
    Renderable(
        base(metallicFactor = 0.0f),
        plugin("normal", "!shader/plugin/normal.terrain.frag"),
        plugin("terrain", "hybridterrain/height.glsl"),
        plugin("albedo", "hybridterrain/albedo.glsl"),
        uniforms(
            "heightTexture" to texture("base-terrain", heightMap),
            "patchTexture" to texture("hybridterrain/color.png", TextureFilter.Nearest),
            "sdf" to texture("hybridterrain/sdf.png", TextureFilter.Linear),
            "road" to texture("infcity/road.jpg"),
            "grassTexture" to texture("texture/grass.jpg")
        ),
        prefab = terrain
    )


    //    for (xx in -10..10) {
//        for (zz in -10..10) {
//            Renderable(
//                base(color = ColorRGBA.Red),
//                mesh = sphere(20f),
//                transform = translate(xx * 1000f, height(heightMap, fbm, xx * 1000f, zz * 1000f), zz * 1000f)
//            )
//        }
//    }
    fun cubTex(prefix: String) = cubeTexture(CubeTextureSide.entries.associateWith { "hybridterrain/tree/$prefix-${it.toString().lowercase()}.jpg" })

//    Billboard(
//        billboard(),
//        base(metallicFactor = 0f, roughnessFactor = 0.9f),
//        radiant(
//            radiantTexture = cubTex("radiant"),
//            radiantNormalTexture = cubTex("radiant-normal"),
//            colorTexture = cubTex("albedo"),
//            normalTexture = cubTex("normal")
//        ),
//        instancing = billboardInstancing(
//            id = "trees",
//            dynamic = false,
//            count = 2000
//        ) {
//            (0 until 2000).forEach {
//                val r = Random(it)
//                val x = r.nextFloat() * 5000f - 600f
//                val z = r.nextFloat() * 3000f - 1610f
//                Instance(
//                    pos = Vec3(x, height(heightMap, fbm, x, z) + 50f, z),
//                    scale = Vec2(100.0f, 100.0f)
//                )
//            }
//        })
}

private fun FrameContext.atmosphere() {
    Sky(fastCloudSky())
    PostProcess(water(waveScale = 3000.0f, transparency = 0.1f), fastCloudSky())
    //PostProcess(fog(color = ColorRGB(0x9BB4C8), density = 0.00003f))
}

private fun FrameContext.gui() =
    Gui {
        Column {
            Filler()
            Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
        }
    }


