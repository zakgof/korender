package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.material.MaterialModifiers.options
import com.zakgof.korender.material.MaterialModifiers.standartUniforms
import com.zakgof.korender.mesh.Meshes.cube
import com.zakgof.korender.material.StandartMaterialOption
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3

@Composable
fun TransparencyExample() = Korender {
    Frame {
        fun semitransparent(color: Color, position: Vec3) = Renderable(
            options(StandartMaterialOption.Color, StandartMaterialOption.NoLight),
            standartUniforms {
                this.color = color
            },
            mesh = cube(),
            transform = scale(5.0f, 5.0f, 0.1f).translate(position),
            transparent = true
        )

        semitransparent(Color(0.5f, 0.5f, 0.0f, 0.0f), Vec3(0f, 0f, 0f))
        semitransparent(Color(0.5f, 0.0f, 0.5f, 0.0f), Vec3(1f, 1f, 1f))
        semitransparent(Color(0.5f, 0.0f, 0.0f, 0.5f), Vec3(-1f, -1f, -1f))
    }
}
