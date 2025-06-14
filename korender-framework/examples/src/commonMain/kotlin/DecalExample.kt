package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

@Composable
fun DecalExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    val materialModifiers = arrayOf(
        base(colorTexture = texture("texture/asphalt-albedo.jpg"), metallicFactor = 0.2f),
        normalTexture(normalTexture = texture("texture/asphalt-normal.jpg")),
    )
    camera = camera(20.z, -1.z, 1.y)
    Frame {
        DirectionalLight(Vec3(1f, -1f, -1f), white(2.0f))
        AmbientLight(white(0.3f))

        DeferredShading {
            val blots = frameInfo.time.toInt().coerceIn(1, 4)
            (0 until blots).forEach {
                val look = (-1.z + it.x - 2.x).normalize()
                val up = 1.y
                val pos = 3.x - look * 2.0f
                Decal(position = pos, look = look, up = up, size = 1.6f, colorTexture = texture("texture/decal.png"))
            }
        }
        Renderable(
            *materialModifiers,
            mesh = sphere(2f),
            transform = translate(3.x),
        )
        Gui {
            Column {
                Filler()
                Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
            }
        }
    }
}