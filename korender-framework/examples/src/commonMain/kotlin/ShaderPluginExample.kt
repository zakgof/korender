package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.declaration.Materials.standard
import com.zakgof.korender.declaration.Meshes.sphere
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.FloatMath.sin
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.y

@Composable
fun ShaderPluginExample() = Korender {
    Frame {
        Renderable(
            mesh = sphere(2.0f),
            material = standard(plugins = mapOf("texture" to "checked.frag")) {
                static("color1", Color(0xFFD0D0))
                static("color2", Color(0x000020))
            },
            transform = Transform().translate(sin(frameInfo.time).y)
        )
    }
}