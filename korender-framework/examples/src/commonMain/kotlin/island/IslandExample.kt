package com.zakgof.korender.examples.island

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.ShaderPlugin
import com.zakgof.korender.ShaderPluginId
import com.zakgof.korender.scope.FrameScope
import com.zakgof.korender.examples.TestExchange
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Transform.Companion.rotate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import island.island
import kotlinx.coroutines.ExperimentalCoroutinesApi

internal lateinit var normalTerrainPlugin: ShaderPlugin
internal lateinit var terrainHeightPlugin: ShaderPlugin
internal lateinit var albedoTerrainPlugin: ShaderPlugin

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun IslandExample() =
    Korender(resourceLoader = { Res.readBytes("files/$it") }) {

        val freeCamera = FreeCamera(this, Vec3(0f, 6000f, -6000f), 1.z - 1.y )

        normalTerrainPlugin = shaderPlugin(ShaderPluginId.NORMAL, "!shader/plugin/normal.terrain.frag")
        terrainHeightPlugin = shaderPlugin(ShaderPluginId.TERRAIN, "island/terrain/shader/height.glsl")
        albedoTerrainPlugin = shaderPlugin(ShaderPluginId.ALBEDO, "island/terrain/shader/albedo.glsl")

        val loader = Loader(this)
        val game = Game(loader)

        OnTouch { game.touch(it) }
        OnKey { game.key(it) }

        OnTouch {freeCamera.touch(it) }
        OnKey { freeCamera.handle(it) }

        Frame {

            TestExchange.report(frameInfo)
            if (loader.loaded()) {
                game.frame(frameInfo.dt)
                gameFrame(game, loader, freeCamera)
            } else {
                loadingScreen(loader.percent())
            }
        }
    }

@OptIn(ExperimentalCoroutinesApi::class)
private fun FrameScope.gameFrame(game: Game, loader: Loader, freeCamera: FreeCamera) {
    projection = projection(2f, 2f * height / width, 2f, 32000f, log())
    camera = camera(game.cameraPos, game.cameraDir, game.cameraUp)

    camera = freeCamera.camera(projection, width, height, frameInfo.dt * 1000f)

    atmosphere()
    island(loader.heightMapLoading.getCompleted(), loader.runwaySeedLoading.getCompleted())
    buildings(loader.deferredBuildings.getCompleted())
    trees(loader.deferredBranches.getCompleted(), loader.deferredCards.getCompleted(), loader.deferredTreeSeeds.getCompleted())
    plane(game.plane.position, game.plane.look, game.plane.up)
    gui(game)
}


private fun FrameScope.plane(position: Vec3, look: Vec3, up: Vec3) = Gltf(
    resource = "island/models/plane.glb",
    transform = rotate(look, up)
        .scale(100.0f)
        .translate(position + 16.y)
)

private fun FrameScope.atmosphere() {
    DeferredShading {
        Shading {
            env = fastCloudSky()
        }
    }
    // AmbientLight(ColorRGB.white(0.5f))
    DirectionalLight(Vec3(3.0f, -3.0f, 1.0f), ColorRGB.white(1.5f)) {
        Cascade(512, 2f, 5000f, 0f to 4000f, hardwarePcf())
        Cascade(512, 2500f, 12000f, 0f to 4000f, hardwarePcf())
    }
    PostProcess(water(waveScale = 3000.0f, transparency = 0.05f, sky = fastCloudSky()))
    PostProcess(fxaa())
    PostProcess(fog(color = ColorRGB(0xB8CAE9), density = 0.00003f)) {
        Sky(fastCloudSky())
    }
}

private fun FrameScope.gui(game: Game) =
    Gui {
        Column {
            Filler()
            Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
            Text(id = "velocity", text = "Speed ${game.plane.velocity.length().toInt()}")
        }
    }

private fun FrameScope.loadingScreen(percent: Int) =
    Gui {
        Column {
            Filler()
            Text(id = "loading", text = "Loading ${percent}%...")
        }
    }



