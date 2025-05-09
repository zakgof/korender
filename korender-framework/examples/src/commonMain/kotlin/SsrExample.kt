package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SsrExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    val env = cubeTexture(
        "cube/room/nx.jpg",
        "cube/room/ny.jpg",
        "cube/room/nz.jpg",
        "cube/room/px.jpg",
        "cube/room/py.jpg",
        "cube/room/pz.jpg"
    )
    Frame {
        val phase = frameInfo.time.toInt() % 3
        DeferredShading {
            if (phase == 1) {
                PostShading(ssr(width = width / 4, height = height / 4, fxaa = true) {
                    maxRayTravel = 25f
                    linearSteps = 18
                    binarySteps = 5
                    envTexture = env
                })
            }
            if (phase == 2) {
                Shading(ibl(env))
            }
        }

        DirectionalLight(Vec3(1f, -1f, 0f).normalize()) {
            // Cascade(512, 5f, 100f, -5f to 5f, pcss(8))
        }
        Sky(cubeSky(env))
        Renderable(
            standart {
                baseColor = ColorRGBA.Red
                pbr.metallic = 0.0f
                pbr.roughness = 0.2f
            },
            mesh = sphere(),
            transform = translate(-2f, -1f, -5f)
        )
        Renderable(
            standart {
                baseColor = ColorRGBA.Green
                pbr.metallic = 0.0f
                pbr.roughness = 0.2f
            },
            mesh = sphere(),
            transform = translate(2f, -1f, -5f)
        )
        Renderable(
            standart {
                baseColorTexture = texture("texture/asphalt-albedo.jpg")
                pbr.metallic = 0.3f
                pbr.roughness = 0.2f
                triplanarScale = 0.4f
            },
            mesh = cube(6f),
            transform = translate(-8.y)
        )
        Gui {
            val mode = when (phase) {
                1 -> "SSR"
                2 -> "IBL"
                else -> ""
            }
            Column {
                Filler()
                Text(id = "mode", text = mode)
                Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
            }
        }
    }
}

private fun FrameContext.scene() {
    AmbientLight(white(0.5f))
    DirectionalLight(Vec3(1.0f, -1.0f, -1.0f).normalize())
    Sky(fastCloudSky())
    Renderable(
        standart {
            baseColor = ColorRGBA.Red
        },
        mesh = sphere(0.5f),
        transform = translate(2.y - 15.z)
    )
}