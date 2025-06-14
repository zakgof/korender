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
            Decal(position = -3.x+2.z, look = -1.z, up = 1.y, size = 0.6f, colorTexture = texture("texture/decal.png"))
            Decal(position = 3.x+2.z, look = -1.z, up = 1.y, size = 0.6f, colorTexture = texture("texture/decal.png"))
        }

        Renderable(
            *materialModifiers,
            mesh = cube(2f),
            transform = translate(-3.x)
        )
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