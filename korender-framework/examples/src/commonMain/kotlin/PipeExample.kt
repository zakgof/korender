package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

@Composable
fun PipeExample() = Korender(resourceLoader = { Res.readBytes("files/$it") }) {
    Frame {
        TestExchange.report(frameInfo)
        camera = camera((-10).z, 1.z, 1.y)
        projection = projection(3f * width / height, 3f, 3f, 100f)
        DirectionalLight(Vec3(1f, -1f, 0.1f))
        Renderable(
            pipe {
                color = ColorRGBA.Blue
            },
            mesh = pipeMesh("pipe", 9, false) {
                cycle {
                    node(-2.x - 2.y, 0.1f)
                    node(-2.x + 2.y, 0.2f)
                    node(2.x + 2.y, 0.3f)
                    node(2.x - 2.y, 0.4f)
                }
                sequence {
                    node(0.y, 0.6f)
                    node(2.y, 0.6f)
                    node(4.y + 1.x, 0.6f)
                    node(5.y, 0.6f)
                    node(-2.y, 1.6f)
                    node(6.y, 1.6f)
                }
            }
        )
        Renderable(
            base { color = ColorRGBA.Red },
            mesh = cube(),
            transform = scale(7f, 1f, 7f).translate(-2.4f.y)
        )
        Gui {
            Column {
                Filler()
                Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}", height = 40f, color = ColorRGBA(0x66FF55A0))
            }
        }
    }
}

