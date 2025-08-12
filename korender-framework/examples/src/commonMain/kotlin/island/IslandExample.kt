package com.zakgof.korender.examples.island

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.CubeTextureSide
import com.zakgof.korender.Image
import com.zakgof.korender.Korender
import com.zakgof.korender.Prefab
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.examples.island.city.CityGenerator
import com.zakgof.korender.examples.island.city.generateBuilding
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA.Companion.white
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.math.roundToInt

fun floatAt(bytes: ByteArray, offset: Int): Float {
    val bits = (bytes[offset + 3].toInt() and 0xFF shl 24) or
            (bytes[offset + 2].toInt() and 0xFF shl 16) or
            (bytes[offset + 1].toInt() and 0xFF shl 8) or
            (bytes[offset].toInt() and 0xFF)
    return Float.fromBits(bits)
}

fun normalizedToWorld(n: Vec3) = Vec3(
    (-0.5f + n.x) * 32f * 512f,
    n.y * 256f * 16f - 256f * 16f * 0.1f + 64f,
    (-0.5f + n.z) * 32f * 512f
)

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun IslandExample() =
    Korender(appResourceLoader = { Res.readBytes(it) }) {

        val deferredBranches = load("files/island/tree/branches.bin") { loadBranches(it) }
        val deferredCards = load("files/island/tree/cards.bin") { loadCards(it) }

        val deferredBuildings = load("files/island/building/buildings.bin") { bytes ->

            val cityGenerator = CityGenerator()

            val size = bytes.size / (2 * 3 * 4)
            (0 until size).forEach { i ->
                val p1 = Vec3(floatAt(bytes, i * (2 * 3 * 4)), floatAt(bytes, i * (2 * 3 * 4) + 4), floatAt(bytes, i * (2 * 3 * 4) + 8))
                val p2 = Vec3(floatAt(bytes, i * (2 * 3 * 4) + 12), floatAt(bytes, i * (2 * 3 * 4) + 16), floatAt(bytes, i * (2 * 3 * 4) + 20))

                val c = (p1 + p2) * 0.5f * 512f
                val halfDim = (p2 - p1) * 0.3f * 512f

                val xoffset = (c - halfDim).x.toInt()
                val yoffset = (c - halfDim).z.toInt()
                val xsize = (halfDim.x * 2f).toInt()
                val ysize = (halfDim.z * 2f).toInt()

                generateBuilding(cityGenerator, xoffset, yoffset, xsize, ysize, i)
            }
            cityGenerator
        }
        val deferredTrees = load("files/island/tree/trees.bin") { bytes ->
            loadBinary(bytes) {
                (0 until bytes.size / 12).map {
                    normalizedToWorld(getVec3())
                }
            }
        }

        val terrain = clipmapTerrainPrefab("terrain", 32.0f, 24, 6)
        val heightMapLoading = loadImage("island/terrain/height.png")
        val fbmLoading = loadImage("!fbm.png")

        val freeCamera = FreeCamera(this, Vec3(23.0f, 4000f, -7000.0f), (1.z - 1.y).normalize(), 1400f)
        OnTouch { freeCamera.touch(it) }
        OnKey { freeCamera.handle(it) }

        Frame {

            // DeferredShading()

            projection = projection(2f, 2f * height / width, 2f, 32000f, log())
            camera = freeCamera.camera(projection, width, height, frameInfo.dt)

            AmbientLight(ColorRGB.white(0.5f))
            DirectionalLight(Vec3(15.0f, -3.0f, 1.0f), ColorRGB.white(1.5f)) {
                Cascade(512, 2f, 3000f, 0f to 4000f, hardwarePcf())
                Cascade(512, 2500f, 12000f, 0f to 4000f, hardwarePcf())
            }

            if (heightMapLoading.isCompleted && fbmLoading.isCompleted) {
                island(heightMapLoading.getCompleted(), fbmLoading.getCompleted(), terrain)
            }

            atmosphere()
            gui()
            if (deferredBuildings.isCompleted) {
                buildings(deferredBuildings.getCompleted())
            }

            if (deferredBranches.isCompleted && deferredTrees.isCompleted && deferredCards.isCompleted) {
                renderTrees(deferredBranches.getCompleted(), deferredCards.getCompleted(), deferredTrees.getCompleted())
            }
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
        plugin("terrain", "island/terrain/shader/height.glsl"),
        plugin("albedo", "island/terrain/shader/albedo.glsl"),
        uniforms(
            "heightTexture" to texture("base-terrain", heightMap),
            "patchTexture" to texture("island/terrain/color.png"),
            "sdf" to texture("island/terrain/sdf.png", TextureFilter.Linear),
            "road" to texture("infcity/road.jpg"),
            "grassTexture" to texture("texture/grass.jpg")
        ),
        prefab = terrain
    )
}

private fun FrameContext.trees(seeds: List<Vec3>) {
    fun cubTex(prefix: String) = cubeTexture(CubeTextureSide.entries.associateWith { "island/tree/$prefix-${it.toString().lowercase()}.jpg" })
    Billboard(
        billboard(),
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
            count = seeds.size
        ) {
            seeds.forEach { it ->
                Instance(
                    pos = Vec3(
                        (-0.5f + it.x) * 32f * 512f,
                        it.y * 256f * 16f - 256f * 16f * 0.1f + 64f,
                        (-0.5f + it.z) * 32f * 512f
                    ),
                    Vec2(512f, 512f)
                )
            }
        })
}

private fun FrameContext.atmosphere() {
    PostProcess(water(waveScale = 3000.0f, transparency = 0.05f), fastCloudSky())
    PostProcess(fog(color = ColorRGB(0xB8CAE9), density = 0.00003f)) {
        Sky(fastCloudSky())
    }
}

private fun FrameContext.buildings(cityGenerator: CityGenerator) {

    val dim = 32f * 512f

    val tr = scale(32f).translate(Vec3(-dim * 0.5f, -100f, -dim * 0.5f))
    val concrete = base(color = white(2.0f), colorTexture = texture("infcity/roof.jpg"), metallicFactor = 0f, roughnessFactor = 1f)

    Renderable(
        concrete,
        plugin("albedo", "island/building/shader/island.window.albedo.frag"),
        mesh = mesh("lw", cityGenerator.lightWindow),
        transform = tr
    )
    Renderable(
        concrete,
        mesh = mesh("rf", cityGenerator.roof),
        transform = tr
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


