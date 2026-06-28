package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.Slider
import com.zakgof.korender.SliderState
import com.zakgof.korender.examples.camera.OrbitCamera
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import com.zakgof.korender.scope.GuiContainerScope

@Composable
fun HbaoExample() = Korender(resourceLoader = { Res.readBytes("files/$it") }) {
    val orbitCamera = OrbitCamera(14.z + 1.5f.y, 0.8f.y)
    OnTouch { orbitCamera.touch(it) }

    val material = base {
        color = ColorRGBA(0.88f, 0.88f, 0.92f, 1f)
        roughnessFactor = 0.95f
        metallicFactor = 0f
    }
    val samples = SliderState(20f, 2f, 128f)
    val radius = SliderState(0.9f, 0.05f, 10f)
    val bias = SliderState(0.025f, 0.0f, 0.25f)
    val intensity = SliderState(1.15f, 0.1f, 3.0f)
    val bradius = SliderState(16f, 1f, 64f)

    Frame {
        TestExchange.report(frameInfo)
        DeferredShading {
            Hbao(
                downsample = 2,
                sampleCount = samples.position.toInt(),
                radius = radius.position,
                bias = bias.position,
                intensity = intensity.position,
                blurRadius = bradius.position
            )
        }
        AmbientLight(white(0.85f))
        camera = orbitCamera.run { camera() }

        Renderable(
            material,
            mesh = cube(1f),
            transform = scale(18f, 0.18f, 18f).translate(0f, -3.1f, 0f)
        )

        val mergedSpheres = listOf(
            Triple(Vec3(0f, -1.2f, 0f), 1.65f, material),
            Triple(Vec3(-1.15f, -0.95f, 0.15f), 1.0f, material),
            Triple(Vec3(1.05f, -0.8f, -0.2f), 0.95f, material),
            Triple(Vec3(0.15f, 0.3f, 0.7f), 0.9f, material),
            Triple(Vec3(0.75f, -0.1f, 1.0f), 0.85f, material),
            Triple(Vec3(-0.8f, -0.15f, -0.9f), 0.85f, material),
            Triple(Vec3(0.0f, 0.95f, -0.25f), 0.75f, material),
        )
        mergedSpheres.forEach { (position, radius, material) ->
            Renderable(
                material,
                mesh = sphere(radius),
                transform = translate(position)
            )
        }

        Gui {
            Row {
                Column {
                    Filler()
                    Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
                }
                Filler()
                Column {
                    Tuner("samples", "Samples ${samples.position.toInt()} ", samples)
                    Tuner("radius", "Radius ${radius.position.fixedDecimals(1)} ", radius)
                    Tuner("bias", "Bias ${bias.position.fixedDecimals(3)} ", bias)
                    Tuner("intensity", "Intensity ${intensity.position.fixedDecimals(2)} ", intensity)
                    Tuner("blur-radius", "Blur Radius ${bradius.position.toInt()} ", bradius)
                }
            }
        }
    }
}

private fun GuiContainerScope.Tuner(id: String, label: String, state: SliderState) {
    Row(padding = 4f) {
        Filler()
        Text(id = "$id.text", height = 36f, text = label)
        Slider("$id.slider", state, width / 2f, 48f)
    }
}
