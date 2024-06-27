package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.declaration.MaterialModifiers.options
import com.zakgof.korender.declaration.MaterialModifiers.standardUniforms
import com.zakgof.korender.declaration.Meshes.cube
import com.zakgof.korender.declaration.StandardMaterialOption
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3

@Composable
fun TransparencyExample() = Korender {
    Frame {
        fun semitransparent(color: Color, position: Vec3) = Renderable(
            options(StandardMaterialOption.Color, StandardMaterialOption.NoLight),
            standardUniforms {
                this.color = color
            },
            mesh = cube(),
            transform = Transform().scale(5.0f, 5.0f, 0.1f).translate(position),
            transparent = true
        )

        semitransparent(Color(0.5f, 0.5f, 0.0f, 0.0f), Vec3(0f, 0f, 0f))
        semitransparent(Color(0.5f, 0.0f, 0.5f, 0.0f), Vec3(1f, 1f, 1f))
        semitransparent(Color(0.5f, 0.0f, 0.0f, 0.5f), Vec3(-1f, -1f, -1f))
    }
}
