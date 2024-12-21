package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.material.MaterialModifiers.standart
import com.zakgof.korender.material.StandartMaterialOption.FixedColor
import com.zakgof.korender.material.StandartMaterialOption.NoLight
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.mesh.Meshes.cube
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun TransparencyExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    Frame {
        fun semitransparent(color: Color, position: Vec3) = Renderable(
            standart(FixedColor, NoLight) {
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
