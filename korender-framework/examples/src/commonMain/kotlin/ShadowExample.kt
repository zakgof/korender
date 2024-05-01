package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.declaration.Materials.standard
import com.zakgof.korender.declaration.Meshes.cube
import com.zakgof.korender.declaration.Meshes.sphere
import com.zakgof.korender.declaration.Textures.texture
import com.zakgof.korender.math.FloatMath.sin
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

@Composable
fun ShadowExample() =
    Korender {
        val material = standard("SHADOW_RECEIVER0", "PCSS") {
            colorTexture = texture("/sand.jpg")
        }
        Scene {
            Light(Vec3(1f, -1f, 1f).normalize())
            Camera(DefaultCamera(Vec3(-2.0f, 3f, 20f), -1.z, 1.y))
            Renderable(
                mesh = cube(1f),
                material = material,
                transform = Transform().scale(8f, 1f, 8f)
            )
            Shadow(mapSize = 1024, cascades = listOf(5.0f, 50.0f)) {
                Renderable(
                    mesh = cube(1.0f),
                    material = material,
                    transform = Transform().translate(2.y).rotate(1.y, frameInfo.time * 0.1f),
                )
                Renderable(
                    mesh = sphere(1.5f),
                    material = material,
                    transform = Transform().translate(Vec3(-5.0f, 3.5f + sin(frameInfo.time), 0.0f)),
                )
            }
        }
    }