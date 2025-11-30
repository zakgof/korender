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
fun SsrExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    val orbitCamera = OrbitCamera(20.z + 4.y, 1.y)
    OnTouch { orbitCamera.touch(it) }
    val env = cubeTexture(CubeTextureSide.entries.associateWith { "cube/room/${it.toString().lowercase()}.jpg" })
    Frame {
        camera = orbitCamera.run { camera() }
        val phase = frameInfo.time.toInt() % 3
        DeferredShading {
            if (phase == 1) {
                PostShading(
                    ssr(downsample = 2, envTexture = env)
                )
            }
            if (phase == 2) {
                Shading(ibl(env))
            }
        }

        AmbientLight(white(if (phase == 0) 0.6f else 0f))
        DirectionalLight(Vec3(1f, -1f, 0f))
        Sky(cubeSky(env))
        Renderable(
            base(color = ColorRGBA.Red, metallicFactor = 0f, roughnessFactor = 0.2f),
            mesh = sphere(),
            transform = translate(-2f, -1f, -4f),
            transparent = false
        )
        Renderable(
            base(color = ColorRGBA.Green, metallicFactor = 0f, roughnessFactor = 0.2f),
            mesh = sphere(),
            transform = translate(2f, -1f, -4f)
        )
        Renderable(
            base(colorTexture = texture("texture/asphalt-albedo.jpg"), metallicFactor = 0.3f, roughnessFactor = 0.2f),
            triplanar(0.4f),
            mesh = cube(6f),
            transform = translate(-8.y)
        )
        Gui {
            val mode = when (phase) {
                1 -> "SSR"
                2 -> "ENV"
                else -> "NO ENV"
            }
            Column {
                Filler()
                Text(id = "mode", text = mode)
                Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
            }
        }
    }
}

