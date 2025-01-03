package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.OrbitCamera
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun TransparencyExample() {
    Korender(appResourceLoader = { Res.readBytes(it) }) {
        val orbitCamera = OrbitCamera(this, 20.z, 0.y)
        OnTouch { orbitCamera.touch(it) }
        Frame {
            camera = orbitCamera.camera(projection, width, height)
            fun semitransparent(color: Color, position: Vec3) = Renderable(
                standart {
                    baseColor = color
                },
                mesh = cube(),
                transform = scale(5.0f, 5.0f, 0.1f).translate(position),
                transparent = true
            )

            semitransparent(Color(0.5f, 0.5f, 0.0f, 0.0f), Vec3(0f, 0f, 0f))
            semitransparent(Color(0.5f, 0.0f, 0.5f, 0.0f), Vec3(1f, 1f, 1f))
            semitransparent(Color(0.5f, 0.0f, 0.0f, 0.5f), Vec3(-1f, -1f, -1f))
        }
    }
}
