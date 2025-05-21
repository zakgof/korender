package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.CubeTextureSide
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.max

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MetallicRoughnessExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    val env = cubeTexture(CubeTextureSide.entries.associateWith { "cube/room/${it.toString().lowercase()}.jpg" })
    Frame {
        projection = frustum(width = 3f * width / height, height = 3f, near = 3f, far = 1000f)
        Sky(cubeSky(env))
        DirectionalLight(Vec3(1.0f, -1.0f, 0.0f), ColorRGB.white(5f))
        AmbientLight(ColorRGB.Black)
        for (m in 0..4) {
            for (r in 0..4) {
                Renderable(
                    base(
                        color = ColorRGBA(0x80A0FFFF),
                        metallicFactor = r / 4.0f,
                        roughnessFactor = max(m / 4.0f, 0.05f)
                    ),
                    ibl(env),
                    mesh = sphere(0.8f),
                    transform = translate((m - 2) * 1.7f, (r - 2) * 1.7f, 8f)
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