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
        projection = frustum(width = 3f * width / height, height = 3f, near = 3f, far = 1000f)
        camera = camera(18.z, -1.z, 1.y)
        Sky(cubeSky(env))
        DirectionalLight(Vec3(1.0f, -1.0f, 0.0f), ColorRGB.white(5f))
        AmbientLight(ColorRGB.Black)
        for (m in 0..40) {
            for (r in 0..40) {
                Renderable(
                    base(
                        color = ColorRGBA(0x80A0FFFF),
                        metallicFactor = r / 40.0f,
                        roughnessFactor = max(m / 40.0f, 0.05f)
                    ),
                    ibl(env),
                    mesh = sphere(0.8f),
                    transform = translate((m - 2) * 0.17f, (r - 2) * 0.17f, 8f)
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