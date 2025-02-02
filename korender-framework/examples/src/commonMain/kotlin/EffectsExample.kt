package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.floor

@OptIn(ExperimentalResourceApi::class)
@Composable
fun EffectsExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    camera = camera(Vec3(0f, 5f, 30f), -1.z, 1.y)
    Frame {
        DirectionalLight(Vec3(1f, -1f, -1f))
        Sky(fastCloudSky())

        fireDemo()
        smokeDemo()
        fireballDemo()

        Filter(water(), fastCloudSky())
        Gui {
            Column {
                Filler()
                Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
            }
        }
    }
}

private fun FrameContext.fireDemo() = Billboard(
    fire {
        yscale = 10f
        xscale = 2f
    },
    position = (-5).x + 5.y,
    transparent = true
)

private fun FrameContext.smokeDemo() {
    val n = 100
    for (i in 1..n) {
        val phase = fract(frameInfo.time * 0.5f + n.toFloat() / i)
        Billboard(
            smoke {
                xscale = 5f * phase + 0.5f
                yscale = 5f * phase + 0.5f
                seed = i / n.toFloat()
                density = 1.0f - phase
            },
            position = (6.0f + phase * phase * 8f + phase * 2f - 4f).y,
            transparent = true
        )
    }
}

private fun FrameContext.fireballDemo() {
    val phase = fract(frameInfo.time)
    Billboard(
        fireball {
            xscale = 8f * phase
            yscale = 8f * phase
            power = phase
        },
        transparent = true,
        position = 5.x + 3.y
    )
}

fun fract(time: Float): Float = time - floor(time)
