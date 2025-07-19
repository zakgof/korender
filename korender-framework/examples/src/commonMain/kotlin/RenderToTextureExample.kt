package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform.Companion.rotate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

@Composable
fun RenderToTextureExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {

    Frame {

        CaptureFrame("timer", 256, 256, camera = camera(10.z, -1.z, 1.y), projection = projection(2f, 2f, 1f, 20f, ortho())) {
            Sky(starrySky(density = 250f))
            Gui {
                Column {
                    Filler()
                    Row {
                        Filler()
                        Text(id = "countdown", text = "${99 - frameInfo.time.toInt().coerceAtMost(99)}", height = 200, color = ColorRGBA.Red)
                        Filler()
                    }
                    Filler()
                }
            }
        }

        AmbientLight(ColorRGB.White)
        DirectionalLight(-1.z, ColorRGB.white(3f))
        Renderable(
            base(
                colorTexture = textureProbe("timer")
            ),
            mesh = cube(2f),
            transform = rotate(Quaternion.fromAxisAngle(Vec3(1f, 1f, 1f).normalize(), frameInfo.time))
        )

        Gui {
            Column {
                Filler()
                Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
            }
        }
    }
}
