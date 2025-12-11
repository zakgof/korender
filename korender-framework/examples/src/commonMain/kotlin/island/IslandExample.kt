package com.zakgof.korender.examples.island

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.Prefab
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Transform.Companion.rotate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import island
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun IslandExample() =
    Korender(appResourceLoader = { Res.readBytes(it) }) {

        val terrain = clipmapTerrainPrefab("terrain", 32.0f, 24, 6)
        val loader = Loader(this)
        val game = Game(loader)

        OnTouch { game.touch(it) }
        OnKey { game.key(it) }

        Frame {
            if (loader.loaded()) {
                game.frame(frameInfo.dt)
                gameFrame(game, loader, terrain)
            } else {
                loadingScreen(loader.percent())
            }
        }
    }

@OptIn(ExperimentalCoroutinesApi::class)
private fun FrameContext.gameFrame(game: Game, loader: Loader, terrain: Prefab) {
    projection = projection(2f, 2f * height / width, 2f, 32000f, log())
    camera = camera(game.cameraPos, game.cameraDir, game.cameraUp)

    island(loader.heightMapLoading.getCompleted(), loader.runwaySeedLoading.getCompleted(), terrain)
    atmosphere()
    buildings(loader.deferredBuildings.getCompleted())
    trees(loader.deferredBranches.getCompleted(), loader.deferredCards.getCompleted(), loader.deferredTreeSeeds.getCompleted())
    plane(game.plane.position, game.plane.look, game.plane.up)
    gui(game)
}


private fun FrameContext.plane(position: Vec3, look: Vec3, up: Vec3) = Gltf(
    resource = "island/models/plane.glb",
    transform = rotate(look, up)
        .scale(100.0f)
        .translate(position + 16.y)
)

private fun FrameContext.atmosphere() {
    AmbientLight(ColorRGB.white(0.5f))
    DirectionalLight(Vec3(3.0f, -3.0f, 1.0f), ColorRGB.white(3.5f)) {
        Cascade(512, 2f, 5000f, 0f to 4000f, hardwarePcf())
        Cascade(512, 2500f, 12000f, 0f to 4000f, hardwarePcf(0.006f))
    }
    PostProcess(water(waveScale = 3000.0f, transparency = 0.05f), fastCloudSky())
    PostProcess(fog(color = ColorRGB(0xB8CAE9), density = 0.00003f)) {
        Sky(fastCloudSky())
    }
}

private fun FrameContext.gui(game: Game) =
    Gui {
        Column {
            Filler()
            Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
            Text(id = "velocity", text = "Speed ${game.plane.velocity.length().toInt()}")
        }
    }

private fun FrameContext.loadingScreen(percent: Int) =
    Gui {
        Column {
            Filler()
            Text(id = "loading", text = "Loading ${percent}%...")
        }
    }


