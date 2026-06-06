package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.CubeTextureSide
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.OrbitCamera
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

@Composable
fun SsrExample() = Korender(resourceLoader = { Res.readBytes("files/$it") }) {
    val orbitCamera = OrbitCamera(20.z + 4.y, 1.y)
    OnTouch { orbitCamera.touch(it) }
    val envTex = cubeTexture(CubeTextureSide.entries.associateWith { "cube/room/${it.toString().lowercase()}.jpg" })
    Frame {
        TestExchange.report(frameInfo)
        camera = orbitCamera.run { camera() }
        val iblSky = cubeSky(envTex)
        DeferredShading {
            Ssr(downsample = 2, envTexture = envTex)
        }

        AmbientLight(white( 0.6f))
        DirectionalLight(Vec3(1f, -1f, 0f))
        Sky(iblSky)
        Renderable(
            base {
                color = ColorRGBA.Red
                metallicFactor = 0f
                roughnessFactor = 0.2f
                env = iblSky
            },
            mesh = sphere(),
            transform = translate(-2f, -1f, -4f),
            transparent = false
        )
        Renderable(
            base {
                color = ColorRGBA.Green
                metallicFactor = 0f
                roughnessFactor = 0.2f
                env = iblSky
            },
            mesh = sphere(),
            transform = translate(2f, -1f, -4f)
        )
        Renderable(
            base {
                colorTexture = texture("texture/asphalt-albedo.jpg")
                metallicFactor = 0.15f
                roughnessFactor = 0.2f
                triplanarScale = 0.4f
                env = iblSky
            },
            mesh = cube(6f),
            transform = translate(-8.y)
        )
        Gui {
            Column {
                Filler()
                Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
            }
        }
    }
}


