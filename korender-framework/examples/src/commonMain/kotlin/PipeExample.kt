package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

@Composable
fun PipeExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    Frame {
        camera = camera((-10).z, 1.z, 1.y)
        projection = projection(3f * width / height, 3f, 3f, 100f)
        DirectionalLight(Vec3(1f, -1f, 0.5f))
        Renderable(
            base(color = ColorRGBA.Blue),
            pipe(),
            mesh = pipeMesh("pipe", 7, false) {
                cycle {
                    node(-2.x - 2.y, 0.1f)
                    node(-2.x + 2.y, 0.2f)
                    node(2.x + 2.y, 0.3f)
                    node(2.x - 2.y, 0.4f)
                }
                sequence {
                    node(0.y, 0.6f)
                    node(2.y, 0.6f)
                    node(4.y+1.x, 0.6f)
                    node(5.y, 0.6f)
                }
            }
        )
        Renderable(
            base(color = ColorRGBA.Red),
            mesh = sphere(0.5f),
            transform = translate(-2.x - 2.y)
        )
        Gui {
            Column {
                Filler()
                Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}", height = 40, color = ColorRGBA(0x66FF55A0))
            }
        }
    }
}
