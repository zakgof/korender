package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MetallicRoughnessExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    Frame {
        Light(Vec3(1.0f, 1.0f, -1.0f).normalize())
        for (m in 0..4) {
            for (r in 0..4) {
                Renderable(
                    standart {
                        baseColor = Color.Red
                        pbr.metallic =  0.1f + (m / 4.0f)
                        pbr.roughness = 0.1f + (r / 4.0f)
                    },
                    mesh = sphere(0.5f),
                    transform = translate((m - 2) * 1.2f, (r - 2) * 1.2f, 0f)
                )
            }
        }
    }
}