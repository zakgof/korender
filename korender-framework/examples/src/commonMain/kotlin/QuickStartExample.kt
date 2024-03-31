package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.declaration.Materials.standard
import com.zakgof.korender.declaration.Meshes.sphere
import com.zakgof.korender.image.Images
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.FloatMath.sin
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.y

@Composable
fun QuickStartExample() = Korender {
    Scene {
        Renderable(
            mesh = sphere(2.0f),
            material = standard("COLOR") {
                color = Color(0.2f, 1.0f, 0.5f + 0.5f * sin(frameInfo.time))
            },
            transform = Transform().translate(sin(frameInfo.time).y)
        )
    }
}