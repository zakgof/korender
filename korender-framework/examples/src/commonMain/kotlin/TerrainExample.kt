package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.impl.geometry.terrain.Clipmaps
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun TerrainExample() =
    Korender(appResourceLoader = { Res.readBytes(it) }) {

        val clipmaps = Clipmaps(this, "terrain", 4, 0)
        val projection = frustum(5f, 5f * height / width, 2f, 9000f)
        Frame {

            camera = camera(Vec3(0f, 64f, 0f), -1.y, 1.z)

            AmbientLight(ColorRGB.white(0.2f))
            DirectionalLight(Vec3(1.0f, -1.0f, 0.0f).normalize(), ColorRGB.white(0.5f))

            val tiles = clipmaps.meshes(camera.position)

            tiles.forEach { tile ->
                Renderable(
                    standart {
                        // baseColor = ColorRGBA(tile.w, 0f, 0f, 1f)
                        // baseColorTexture = texture("terrain/ground.png")
                        pbr.metallic = 0.0f
                        set("heightTexture", texture("terrain/base-terrain.jpg", TextureFilter.MipMap, TextureWrap.MirroredRepeat))
                        set("tileOffsetAndScale", tile.offsetAndScale)
                    },
                    vertex("!shader/terrain.vert"),
                    defs("TERRAIN"),
                    mesh = tile.mesh
                )
            }
            Sky(fastCloudSky())
            //PostProcess(water(), fastCloudSky())
            Gui {
                Column {
                    Filler()
                    Text(id = "fps", text = "TILES ${tiles.size} | FPS ${frameInfo.avgFps.toInt()}")
                }
            }
        }
    }