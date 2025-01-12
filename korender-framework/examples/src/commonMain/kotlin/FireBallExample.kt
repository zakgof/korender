package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.floor

@OptIn(ExperimentalResourceApi::class)
@Composable
fun FireBallExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    camera = camera(Vec3(0f, 2f, 20f), -1.z, 1.y)
    Frame {
        AmbientLight(Color.White)
        val phase = fract(frameInfo.time)
        Billboard(
            fireball {
                xscale = 8f * phase
                yscale = 8f * phase
                power = phase
            },
            transparent = true
        )
        Renderable(
            standart {
                baseColorTexture = texture("texture/asphalt-albedo.jpg")
                triplanarScale = 0.2f
            },
            mesh = cube(1f),
            transform = scale(9f).translate(-9.y)
        )
    }
}

fun fract(time: Float): Float = time - floor(time)
