package com.zakgof.korender.examples.island

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Transform.Companion.rotate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import island
import kotlinx.coroutines.ExperimentalCoroutinesApi
import loadRunway

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun IslandExample() =
    Korender(appResourceLoader = { Res.readBytes(it) }) {


        val terrain = clipmapTerrainPrefab("terrain", 32.0f, 24, 6)
        val heightMapLoading = loadImage("island/terrain/height.png")
        val runwaySeedLoading = load("files/island/terrain/runway.bin") { loadRunway(it) }
        val fbmLoading = loadImage("!fbm.png")
        val heightFunc = Height(heightMapLoading)

        val deferredBuildings = load("files/island/building/buildings.bin") { loadBuildings(it) }
        val deferredBranches = load("files/island/tree/branches.bin") { loadBranches(it) }
        val deferredCards = load("files/island/tree/cards.bin") { loadCards(it) }
        val deferredTreeSeeds = load("files/island/tree/trees.bin") { loadTreeSeeds(heightFunc, it) }


        val freeCamera = FreeCamera(this, Vec3(23.0f, 4000f, -7000.0f), (1.z - 1.y).normalize(), 1400f)
        OnTouch { freeCamera.touch(it) }
        OnKey { freeCamera.handle(it) }

        Frame {

            // DeferredShading()

            projection = projection(2f, 2f * height / width, 2f, 32000f, log())
            camera = freeCamera.camera(projection, width, height, frameInfo.dt)

            AmbientLight(ColorRGB.white(0.5f))
            DirectionalLight(Vec3(3.0f, -3.0f, 1.0f), ColorRGB.white(3.5f)) {
                Cascade(512, 2f, 5000f, 0f to 4000f, hardwarePcf())
                Cascade(512, 2500f, 12000f, 0f to 4000f, hardwarePcf(0.006f))
            }

            if (heightMapLoading.isCompleted && fbmLoading.isCompleted && runwaySeedLoading.isCompleted) {
                island(heightMapLoading.getCompleted(), runwaySeedLoading.getCompleted(), terrain)

                val rw = runwaySeedLoading.getCompleted()
                val rw1 = heightFunc.texToWorld(rw.first, 64.0f)
                val rw2 = heightFunc.texToWorld(rw.second, 64.0f)

                val pos = rw1 + (rw2 - rw1) * 0.1f
                Gltf(
                    "island/models/plane.glb",
                    rotate((rw2 - rw1).normalize(), 1.y)
                        .scale(100.0f)
                        .translate(pos)
                )
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


