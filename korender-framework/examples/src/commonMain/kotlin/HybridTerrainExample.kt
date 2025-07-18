package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.CubeTextureSide
import com.zakgof.korender.Image
import com.zakgof.korender.Korender
import com.zakgof.korender.Prefab
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlin.math.roundToInt


class FileLoader<T>(resourceLoader: ResourceLoader, file: String, private val loader: (ByteArray) -> T) {
    var loaded: T? = null
    val deferred = CoroutineScope(Dispatchers.Default).async { resourceLoader(file) }

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: T?
        get() {
            if (loaded == null && deferred.isCompleted) {
                loaded = loader(deferred.getCompleted())
            }
            return loaded
        }
}

fun floatAt(bytes: ByteArray, offset: Int): Float {
    val bits = (bytes[offset + 3].toInt() and 0xFF shl 24) or
            (bytes[offset + 2].toInt() and 0xFF shl 16) or
            (bytes[offset + 1].toInt() and 0xFF shl 8) or
            (bytes[offset].toInt() and 0xFF)
    return Float.fromBits(bits)
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun HybridTerrainExample() =
    Korender(appResourceLoader = { Res.readBytes(it) }) {

        val buildingsLoader = FileLoader({ Res.readBytes(it) }, "files/hybridterrain/buildings.bin") { bytes ->
            val size = bytes.size / (2 * 3 * 4)
            List(size) { i ->
                val p1 = Vec3(floatAt(bytes, i * (2 * 3 * 4)), floatAt(bytes, i * (2 * 3 * 4) + 4), floatAt(bytes, i * (2 * 3 * 4) + 8))
                val p2 = Vec3(floatAt(bytes, i * (2 * 3 * 4) + 12), floatAt(bytes, i * (2 * 3 * 4) + 16), floatAt(bytes, i * (2 * 3 * 4) + 20))
                p1 to p2
            }
        }

        val terrain = clipmapTerrainPrefab("terrain", 32.0f, 24, 6)
        val heightMapLoading = loadImage("hybridterrain/height.png")
        val fbmLoading = loadImage("!fbm.png")

        val freeCamera = FreeCamera(this, Vec3(23.0f, 4000f, -7000.0f), (1.z - 1.y).normalize(), 1400f)
        OnTouch { freeCamera.touch(it) }
        OnKey { freeCamera.handle(it) }

        Frame {
            projection = projection(5f, 5f * height / width, 2f, 32000f)
            camera = freeCamera.camera(projection, width, height, frameInfo.dt)

            AmbientLight(ColorRGB.white(0.2f))
            DirectionalLight(Vec3(1.0f, -1.0f, 0.0f), ColorRGB.white(1.5f))

            if (heightMapLoading.isCompleted && fbmLoading.isCompleted) {
                island(heightMapLoading.getCompleted(), fbmLoading.getCompleted(), terrain)
            }

            atmosphere()
            gui()
            buildingsLoader.data?.let { buildings(it) }
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
            "patchTexture" to texture("hybridterrain/color.png", TextureFilter.Linear),
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

private fun FrameContext.buildings(buildings: List<Pair<Vec3, Vec3>>) {
    Renderable(
        base(color = ColorRGBA.Red),
        mesh = sphere(300f),
        transform = translate(400.y)
    )
    Renderable(
        base(color = ColorRGBA.Blue),
        mesh = cube(),
        instancing = instancing("bldngz", 256, true) {
            buildings.forEach {
                val center = Vec3(
                    (-0.5f + (it.first.x + it.second.x) * 0.5f) * 32f * 512f,
                    (it.first.y + it.second.y) * 0.5f * 500f,
                    (-0.5f + (it.first.z + it.second.z) * 0.5f) * 32f * 512f
                )
                val scale = (it.second - it.first) * 32f * 512f * 0.5f
                Instance(
                    scale(scale.x,
                        (it.second.y - it.first.y) * 500,
                        scale.z)
                        .translate(center)
                )
            }
            // Instance(scale(500.0f))
        }
    )

}

private fun FrameContext.gui() =
    Gui {
        Column {
            Filler()
            Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
            Text(id = "debug", text = "Debug ${(camera.position - 400.y).length().toInt()}")
        }
    }


