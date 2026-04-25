package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.OrbitCamera
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

@Composable
fun SsaoExample() = Korender(resourceLoader = { Res.readBytes("files/$it") }) {
    val orbitCamera = OrbitCamera(14.z + 1.5f.y, 0.8f.y)
    OnTouch { orbitCamera.touch(it) }

    val sphereMaterial = base {
        color = ColorRGBA(0.88f, 0.88f, 0.92f, 1f)
        roughnessFactor = 0.95f
        metallicFactor = 0f
    }
    val accentMaterial = base {
        color = ColorRGBA(0.72f, 0.78f, 0.98f, 1f)
        roughnessFactor = 0.9f
        metallicFactor = 0f
    }
    val floorMaterial = base {
        color = ColorRGBA(0.16f, 0.16f, 0.18f, 1f)
        roughnessFactor = 1f
        metallicFactor = 0f
    }

    Frame {
        TestExchange.report(frameInfo)
        DeferredShading {
            Ssao()
        }
        AmbientLight(white(0.85f))
        camera = orbitCamera.run { camera() }

        Renderable(
            floorMaterial,
            mesh = cube(1f),
            transform = scale(18f, 0.18f, 18f).translate(0f, -3.1f, 0f)
        )

        val mergedSpheres = listOf(
            Triple(Vec3(0f, -1.2f, 0f), 1.65f, sphereMaterial),
            Triple(Vec3(-1.15f, -0.95f, 0.15f), 1.0f, accentMaterial),
            Triple(Vec3(1.05f, -0.8f, -0.2f), 0.95f, sphereMaterial),
            Triple(Vec3(0.15f, 0.3f, 0.7f), 0.9f, accentMaterial),
            Triple(Vec3(0.75f, -0.1f, 1.0f), 0.85f, sphereMaterial),
            Triple(Vec3(-0.8f, -0.15f, -0.9f), 0.85f, accentMaterial),
            Triple(Vec3(0.0f, 0.95f, -0.25f), 0.75f, sphereMaterial),
        )
        mergedSpheres.forEach { (position, radius, material) ->
            Renderable(
                material,
                mesh = sphere(radius),
                transform = translate(position)
            )
        }

        Gui {
            Column {
                Filler()
                Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
            }
        }
    }
}
