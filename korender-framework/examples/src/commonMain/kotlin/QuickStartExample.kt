package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.material.MaterialModifiers.standart
import com.zakgof.korender.material.StandartMaterialOption
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.FloatMath.sin
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.y
import com.zakgof.korender.mesh.Meshes.sphere

@Composable
fun QuickStartExample() = Korender {
    Frame {
        Renderable(
            standart(StandartMaterialOption.Color) {
                color = Color(1.0f, 0.2f, 1.0f, 0.5f + 0.5f * sin(frameInfo.time))
            },
            mesh = sphere(2.0f),
            transform = translate(sin(frameInfo.time).y)
        )
    }
}