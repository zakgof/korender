package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform.Companion.rotate
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.z

@Composable
fun BasicShapesExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    val material = base(color = ColorRGBA.Blue)
    val freeCamera = FreeCamera(this, Vec3(0f, -2f, 20f), (-1).z)
    OnTouch { freeCamera.touch(it) }
    OnKey { freeCamera.handle(it) }
    Frame {
        val rot = rotate(Quaternion.fromAxisAngle(Vec3(1f, 1f, 0f).normalize(), frameInfo.time))
        camera = freeCamera.camera(projection, width, height, frameInfo.dt)
        DirectionalLight(Vec3(1f, -1f, 0f))

        Renderable(
            material,
            mesh = quad(),
            transform = translate(0.x)
        )
        Renderable(
            material,
            mesh = disk(),
            transform = rot.translate(-2.5f.x)
        )
        Renderable(
            material,
            mesh = cone(2f, 1f),
            transform = rot.translate(-5.x)
        )
        Renderable(
            material,
            mesh = cube(1f),
            transform = translate(0.x)
        )

        Gui {
            Column {
                Filler()
                Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}", height = 40, color = ColorRGBA(0x66FF55A0))
            }
        }
    }
}
