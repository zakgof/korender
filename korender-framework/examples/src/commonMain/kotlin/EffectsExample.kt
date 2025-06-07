package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.floor

@Composable
fun EffectsExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    camera = camera(Vec3(0f, 5f, 30f), -1.z, 1.y)
    Frame {
        DirectionalLight(Vec3(1f, -1f, -1f))
        Sky(fastCloudSky())

        fireDemo()
        smokeDemo()
        fireballDemo()

        PostProcess(water(), fastCloudSky())
        Gui {
            Column {
                Filler()
                Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
            }
        }
    }
}

private fun FrameContext.fireDemo() = Billboard(
    billboard(
        position = (-5).x + 5.y,
        scale = Vec2(2f, 10f)
    ),
    fire(),
    transparent = true
)

private fun FrameContext.smokeDemo() {
    val n = 100
    for (i in 1..n) {
        val phase = fract(frameInfo.time * 0.5f + n.toFloat() / i)
        Billboard(
            billboard(
                position = (6.0f + phase * phase * 8f + phase * 2f - 4f).y,
                scale = Vec2(5f * phase + 0.5f, 5f * phase + 0.5f)
            ),
            smoke(seed = i / n.toFloat(), density = 1.0f - phase),
            transparent = true
        )
    }
}

private fun FrameContext.fireballDemo() {
    val phase = fract(frameInfo.time)
    Billboard(
        billboard(
            position = 5.x + 3.y,
            scale = Vec2(8f * phase, 8f * phase)
        ),
        fireball(power = phase),
        transparent = true
    )
}

fun fract(time: Float): Float = time - floor(time)
