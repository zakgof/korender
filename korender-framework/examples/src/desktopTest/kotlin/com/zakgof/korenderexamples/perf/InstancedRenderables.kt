package com.zakgof.korenderexamples.perf

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.CubeTextureSide
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.TestExchange
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

@Composable
fun InstancedRenderables(dynamic: Boolean) = Korender(resourceLoader = { Res.readBytes("files/$it") }) {
    val env = cubeTexture(CubeTextureSide.entries.associateWith { "cube/room/${it.toString().lowercase()}.jpg" })
    Frame {

        val w = 100
        val h = 100

        TestExchange.report(frameInfo)

        projection = projection(width = 3f * width / height, height = 3f, near = 3f, far = 1000f)
        camera = camera(18.z, -1.z, 1.y)
        val sky = cubeSky(env)
        Sky(sky)
        DirectionalLight(Vec3(1.0f, -1.0f, 0.0f), ColorRGB.white(3f))
        AmbientLight(ColorRGB.Black)

        Renderable(
            base {
                color = ColorRGBA(0x80A0FFFF)
                metallicFactor = 0.5f
                roughnessFactor = 0.5f
                ibl = sky
            },
            mesh = sphere(4f / w),
            instancing = instancing("10K", w * h, dynamic) {
                for (m in 0 until w) {
                    for (r in 0 until h) {
                        Instance(
                            transform = translate((m - w / 2) * 8f / w, (r - h / 2) * 8f / h, 8f)
                        )
                    }
                }
            }
        )
        Gui {
            Column {
                Filler()
                Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
            }
        }
    }
}


