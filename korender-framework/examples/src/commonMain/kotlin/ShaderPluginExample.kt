package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.declaration.MaterialModifiers.plugin
import com.zakgof.korender.declaration.MaterialModifiers.standardUniforms
import com.zakgof.korender.declaration.Meshes.sphere
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.FloatMath.sin
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.y

@Composable
fun ShaderPluginExample() = Korender {
    Frame {
        Renderable(
            plugin("texture", "checked.frag"),
            standardUniforms {
                static("color1", Color(0xFFFFFF20))
                static("color2", Color(0xFF8080FF))
            },
            mesh = sphere(2.0f),
            transform = Transform().translate(sin(frameInfo.time).y)
        )
    }
}