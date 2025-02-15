package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.sin

@OptIn(ExperimentalResourceApi::class)
@Composable
fun TerrainExample() =
    Korender(appResourceLoader = { Res.readBytes(it) }) {
        val materialModifier = standart {
            baseColorTexture = texture("texture/asphalt-albedo.jpg")
            normalTexture = texture("texture/asphalt-normal.jpg")
            pbr.metallic = 0.2f
        }
        val roam = Roam(8, 32) { x, z -> sin(x * 0.001f) + sin(z * 0.001f) * (1f - (x - 2048f) * (x - 2048f) / 2048f / 2048f) * (1f - (z - 2048f) * (z - 2048f) / 2048f / 2048f) }
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
        projection = frustum(5f, 5f * height / width, 1f, 2200f)
        Frame {

            camera = camera(Vec3(256 * 32 * 0.5f + frameInfo.time * 100f, 2048f, 256 * 32 * 0.5f + 150f), -1.y, 1.z)

            AmbientLight(ColorRGB.White)
            val tiles = roam.update(camera.position, 0.05f)
            tiles.forEach { tile ->
                Renderable(
                    materialModifier, mesh = mesh,
                    transform = scale(1.shl(tile.level).toFloat(), 1f, tile.size().toFloat())
                        .translate(Vec3(36f * tile.x, 0f, 36f * tile.z))
                )
            }
            Gui {
                Column {
                    Filler()
                    Text(id = "fps", text = "TILES ${tiles.size} FPS ${frameInfo.avgFps.toInt()}")
                }
            }
        }
    }