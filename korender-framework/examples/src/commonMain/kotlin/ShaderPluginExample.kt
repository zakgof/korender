package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.y
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.sin

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ShaderPluginExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    Frame {
        Renderable(
            plugin("texture", "checked.frag"),
            standart {
                set("color1", ColorRGBA(0xFFFFFF20))
                set("color2", ColorRGBA(0xFF8080FF))
            },
            mesh = sphere(2.0f),
            transform = translate(sin(frameInfo.time).y)
        )
    }
}