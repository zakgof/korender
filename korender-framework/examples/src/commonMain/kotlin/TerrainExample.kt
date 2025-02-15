package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun TerrainExample() =
    Korender(appResourceLoader = { Res.readBytes(it) }) {
        val materialModifier = standart {
            baseColorTexture = texture("texture/asphalt-albedo.jpg")
            normalTexture = texture("texture/asphalt-normal.jpg")
            pbr.metallic = 0.2f
        }
        val mesh = customMesh("block", 33 * 33, 32 * 32 * 6, POS, NORMAL, TEX) {
            for (x in 0..32) {
                for (z in 0..32) {
                    pos(x.toFloat(), 0f, z.toFloat()).normal(1.y).tex(x.toFloat() / 32, z.toFloat() / 32)
                }
            }
            for (x in 0..<32) {
                for (z in 0..<32) {
                    val b = x * 33 + z
                    index(b, b + 1, b + 34, b, b + 34, b + 33)
                }
            }
        }
        camera = camera(Vec3(256*32*0.5f, 1024f, 256*32*0.5f), -1.y, 1.z)
        projection = frustum(5f, 5f * height / width, 1f, 1200f)
        Frame {
            AmbientLight(ColorRGB.White)
            for(x in 0 until 256) {
                for(z in 0 until 256) {
                    Renderable(materialModifier, mesh = mesh, transform = translate(Vec3(36f * x, 0f, 36f * z)))
                }
            }
            Gui {
                Column {
                    Filler()
                    Text(id = "fps", text = "FPS ${frameInfo.avgFps}")
                }
            }
        }
    }