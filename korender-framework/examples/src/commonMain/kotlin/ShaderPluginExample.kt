package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.material.MaterialModifiers.plugin
import com.zakgof.korender.material.MaterialModifiers.standartUniforms
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.FloatMath.sin
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.y
import com.zakgof.korender.mesh.Meshes.sphere

@Composable
fun ShaderPluginExample() = Korender {
    Frame {
        Renderable(
            plugin("texture", "checked.frag"),
            standartUniforms {
                static("color1", Color(0xFFFFFF20))
                static("color2", Color(0xFF8080FF))
            },
            mesh = sphere(2.0f),
            transform = translate(sin(frameInfo.time).y)
        )
    }
}