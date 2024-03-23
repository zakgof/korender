package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.material.Materials.standard
import com.zakgof.korender.declaration.MeshDeclarations.sphere
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.FloatMath.sin
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.y
import com.zakgof.korender.projection.FrustumProjection

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