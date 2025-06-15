package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import kotlin.random.Random

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
            val blots = (frameInfo.time * 6f).toInt().coerceIn(1, 12)
            (0 until blots).forEach {
                val r = Random(it)
                val look = Vec3(r.nextFloat() - 0.5f, r.nextFloat() - 0.5f, -0.6f).normalize()
                val up = ((look % 1.y) % look).normalize()
                val pos = -look * 4f
                Decal(
                    base(
                        colorTexture = texture("texture/decal.png"),
                        metallicFactor = 0.2f
                    ),
                    position = pos, look = look, up = up, size = 1.6f
                )
            }
        }
        Renderable(
            *materialModifiers,
            mesh = sphere(4f)
        )
        Gui {
            Column {
                Filler()
                Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
            }
        }
    }
}