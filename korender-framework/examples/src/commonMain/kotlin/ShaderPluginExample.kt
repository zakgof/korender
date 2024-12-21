package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.material.MaterialModifiers.plugin
import com.zakgof.korender.material.MaterialModifiers.standart
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.y
import com.zakgof.korender.mesh.Meshes.sphere
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.sin

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ShaderPluginExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    Frame {
        Renderable(
            plugin("texture", "!files/checked.frag"),
            standart {
                static("color1", Color(0xFFFFFF20))
                static("color2", Color(0xFF8080FF))
            },
            mesh = sphere(2.0f),
            transform = translate(sin(frameInfo.time).y)
        )
    }
}