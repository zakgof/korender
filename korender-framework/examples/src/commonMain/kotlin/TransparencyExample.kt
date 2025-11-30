package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.OrbitCamera
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

@Composable
fun TransparencyExample() {
    Korender(appResourceLoader = { Res.readBytes(it) }) {
        val orbitCamera = OrbitCamera(20.z, 0.y)
        OnTouch { orbitCamera.touch(it) }
        Frame {
            AmbientLight(ColorRGB.White)
            camera = orbitCamera.run { camera() }
            fun semitransparent(color: ColorRGBA, position: Vec3) = Renderable(
                base(color = color),
                mesh = cube(),
                transform = scale(5.0f, 5.0f, 0.1f).translate(position),
                transparent = true
            )

            semitransparent(ColorRGBA(0.5f, 0.0f, 0.0f, 0.5f), Vec3(0f, 0f, 0f))
            semitransparent(ColorRGBA(0.0f, 0.5f, 0.0f, 0.5f), Vec3(1f, 1f, 1f))
            semitransparent(ColorRGBA(0.0f, 0.0f, 0.5f, 0.5f), Vec3(-1f, -1f, -1f))
        }
    }
}
