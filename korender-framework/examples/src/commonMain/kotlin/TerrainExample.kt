package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.Korender
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.sin

@OptIn(ExperimentalResourceApi::class)
@Composable
fun TerrainExample() =
    Korender(appResourceLoader = { Res.readBytes(it) }) {
        val treeLevels = 8
        val tilePower = 13 - treeLevels
        val tileSize = 1 shl tilePower
        val roam = Roam(treeLevels, tileSize) { x, z -> sin(x * 0.001f) + sin(z * 0.001f) * (1f - (x - 2048f) * (x - 2048f) / 2048f / 2048f) * (1f - (z - 2048f) * (z - 2048f) / 2048f / 2048f) }
        val mesh = customMesh("block", (tileSize + 1) * (tileSize + 1), tileSize * tileSize * 6, POS, TEX) {
            for (x in 0..tileSize) {
                for (z in 0..tileSize) {
                    pos(x.toFloat(), 0f, z.toFloat()).tex(x.toFloat() / tileSize, z.toFloat() / tileSize)
                }
            }
            for (x in 0..<tileSize) {
                for (z in 0..<tileSize) {
                    val b = x * (tileSize + 1) + z
                    index(b, b + 1, b + (tileSize + 2), b, b + (tileSize + 2), b + (tileSize + 1))
                }
            }
        }
        var prevtiles = mutableSetOf<Roam.Tile>()
        projection = frustum(5f, 5f * height / width, 8f, 9000f)
        Frame {

            camera = camera(Vec3(4095f, 384f, 8192f - (35f + frameInfo.time) * 100f), (-1.y + 2.z).normalize(), (2.y + 1.z).normalize())

            AmbientLight(ColorRGB.white(0.5f))
            DirectionalLight(Vec3(1.0f, -1.0f, 0.0f).normalize(), ColorRGB.white(3.0f))

            val tiles = roam.tiles(camera.position, 4.5f)

            prevtiles.removeAll(tiles)
            tiles
                .filter { it == Roam.Tile(120, 56, 3) }
                .forEach {
                    println("Super tile ${it}  ${it.w}")
                }
            prevtiles = tiles.toMutableSet()

            tiles.forEach { tile ->
                Renderable(
                    standart {
                        // baseColor = ColorRGBA(tile.w, 0f, 0f, 1f)
                        baseColorTexture = texture("terrain/ground.png")
                        pbr.metallic = 0.0f
                        set("heightTexture", texture("terrain/hfrg.png", TextureFilter.MipMap, TextureWrap.MirroredRepeat))
                        set("tileOffsetAndScale", Vec3(tile.x.toFloat(), tile.z.toFloat(), tile.size().toFloat()))
                        set("antipop", tile.w)
                    },
                    vertex("!shader/terrain.vert"),
                    defs("TERRAIN"),
                    mesh = mesh
                )
            }
            Sky(fastCloudSky())
            PostProcess(water(), fastCloudSky())
            Gui {
                Column {
                    Filler()
                    Text(id = "fps", text = "TILES ${tiles.size} | FPS ${frameInfo.avgFps.toInt()}")
                }
            }
        }
    }