package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.CubeTextureSide
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import kotlin.math.max

@Composable
fun MetallicRoughnessExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    val env = cubeTexture(CubeTextureSide.entries.associateWith { "cube/room/${it.toString().lowercase()}.jpg" })
    Frame {

        projection = projection(width = 3f * width / height, height = 3f, near = 3f, far = 1000f)
        camera = camera(18.z, -1.z, 1.y)
        val sky = cubeSky(env)
        Sky(sky)
        DirectionalLight(Vec3(1.0f, -1.0f, 0.0f), ColorRGB.white(3f))
        AmbientLight(ColorRGB.Black)
        for (m in 0..100) {
            for (r in 0..100) {
                Renderable(
                    base {
                        color = ColorRGBA(0x80A0FFFF)
                        metallicFactor = r / 100.0f
                        roughnessFactor = max(m / 100.0f, 0.05f)
                        ibl = sky
                    },
                    mesh = sphere(0.03f),
                    transform = translate((m - 50) * 1.7f * 0.03f, (r - 50) * 1.7f * 0.03f, 8f)
                )
            }
        }
        Gui {
            Column {
                Filler()
                Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
            }
        }
    }
}
