package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x

@Composable
fun RetentionExample() {
    Korender(resourceLoader = { Res.readBytes("files/$it") }) {
        Frame {
            DirectionalLight(Vec3(1.0f, -1.0f, -1.0f), white(3f))
            if (frameInfo.time < 2f) {
                Renderable(
                    material = base { colorTexture = texture("texture/asphalt-albedo.jpg") },
                    mesh = sphere(1f)
                )
                Node(
                    transform = translate(2.x),
                    retentionPolicy = time(2f)
                ) {
                    Renderable(
                        material = base { colorTexture = texture("texture/grass.jpg") },
                        mesh = sphere(1f)
                    )
                }
                Node(
                    transform = translate((-2).x),
                    retentionPolicy = time(4f)
                ) {
                    Renderable(
                        material = base { colorTexture = texture("texture/splat.png") },
                        mesh = sphere(1f)
                    )
                }

            }
        }
    }
}
