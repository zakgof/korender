package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import com.zakgof.korender.projection.FrustumProjection

@Composable
fun ShadowExample() {
    Korender() {
        camera = DefaultCamera(Vec3(-2.0f, 3f, 20f), -1.z, 1.y)
        light = Vec3(1f, -1f, 1f).normalize()
        onResize = {
            projection = FrustumProjection(
                width = 5f * width / height,
                height = 5f,
                near = 10f,
                far = 1000f
            )
        }
        /*
        val material = standard("SHADOW_RECEIVER", "PCSS") {
            colorFile = "/sand.jpg"
        }
        Scene {
            Renderable (
                name = "plate",
                mesh = cube(1f) {
                    transformer = Transform().scale(8f, 1f, 5f).translate(-1.6f.y)
                },
                material = material
            )
            Renderable (
                name = "rcube",
                mesh = cube(1.5f) {
                    transformer = Transform().scale(8f, 1f, 5f).translate(-1.6f.y)
                },
                material = material,
                transform = Transform().rotate(1.x, -FloatMath.PIdiv2).rotate(1.y, frameInfo.time * 0.1f),
                shadowCaster = true
            )
            Renderable (
                name = "rsphere",
                mesh = sphere(1.5f) {
                    transformer = Transform().scale(8f, 1f, 5f).translate(-1.6f.y)
                },
                material = material,
                transform = Transform().translate(Vec3(-4.0f, 2.0f + sin(frameInfo.time), 0.0f)),
                shadowCaster = true
            )
        } */
    }
}