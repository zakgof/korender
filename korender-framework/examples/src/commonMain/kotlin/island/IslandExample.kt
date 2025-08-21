package com.zakgof.korender.examples.island

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Image
import com.zakgof.korender.Korender
import com.zakgof.korender.Prefab
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import kotlinx.coroutines.ExperimentalCoroutinesApi

fun normalizedToWorld(n: Vec3) = Vec3(
    (-0.5f + n.x) * 32f * 512f,
    n.y * 256f * 16f - 256f * 16f * 0.1f + 64f,
    (-0.5f + n.z) * 32f * 512f
)

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun IslandExample() =
    Korender(appResourceLoader = { Res.readBytes(it) }) {

        val deferredBuildings = load("files/island/building/buildings.bin") { loadBuildings(it) }
        val deferredBranches = load("files/island/tree/branches.bin") { loadBranches(it) }
        val deferredCards = load("files/island/tree/cards.bin") { loadCards(it) }
        val deferredTreeSeeds = load("files/island/tree/trees.bin") { loadTreeSeeds(it) }

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
            DirectionalLight(Vec3(3.0f, -3.0f, 1.0f), ColorRGB.white(3.5f)) {
               Cascade(512, 2f, 5000f, 0f to 2000f, hardwarePcf())
               Cascade(512, 2500f, 12000f, 0f to 2000f, hardwarePcf(0.006f))
            }

            if (heightMapLoading.isCompleted && fbmLoading.isCompleted) {
                island(heightMapLoading.getCompleted(), terrain)
            }

            atmosphere()
            gui()
            if (deferredBuildings.isCompleted) {
                buildings(deferredBuildings.getCompleted())
            }

            if (deferredBranches.isCompleted && deferredTreeSeeds.isCompleted && deferredCards.isCompleted) {
                trees(deferredBranches.getCompleted(), deferredCards.getCompleted(), deferredTreeSeeds.getCompleted())
            }
        }
    }

private fun FrameContext.island(heightMap: Image, terrain: Prefab) {
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
        defs("NO_SHADOW_CAST"),
        prefab = terrain
    )
}

private fun FrameContext.atmosphere() {
    PostProcess(water(waveScale = 3000.0f, transparency = 0.05f), fastCloudSky())
    PostProcess(fog(color = ColorRGB(0xB8CAE9), density = 0.00003f)) {
        Sky(fastCloudSky())
    }
}

private fun FrameContext.gui() =
    Gui {
        Column {
            Filler()
            Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
            Text(id = "debug", text = "Debug ${(camera.position - 400.y).length().toInt()}")
        }
    }


