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
        DeferredShading(ssr {
            samples = 16
            envTexture = env
        })
        DirectionalLight(Vec3(1f, -1f, 0f).normalize())
        Sky(cubeSky(env))
        Renderable(
            standart {
                baseColor = ColorRGBA.Red
                pbr.metallic = 0.0f
                pbr.roughness = 0.2f
            },
            mesh = sphere(),
            transform = translate(0f, -3f, -3f)
        )
        Renderable(
            standart {
                baseColorTexture = texture("texture/asphalt-albedo.jpg")
                pbr.metallic = 0.8f
                pbr.roughness = 0.2f
            },
            mesh = cube(1f),
            transform = translate(-5.y).scale(100f, 1f, 100f)
        )
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