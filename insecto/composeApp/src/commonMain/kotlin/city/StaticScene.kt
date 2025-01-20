package city

import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Color.Companion.White
import com.zakgof.korender.math.Color.Companion.white
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.y


class StaticScene(private val kc: KorenderContext) {

    private val cityTriangulation = cityTriangulation()
    private val roads = roads()

    private fun Triangulation.toCustomMesh(id: String) =
        kc.customMesh(id, this.points.size, this.indexes.size, POS, NORMAL, TEX) {
            pos(*points.toTypedArray())
            normal(*normals.toTypedArray())
            tex(*texs.toTypedArray())
            index(*indexes.toIntArray())
        }

    private val windowsMesh = cityTriangulation.lw().toCustomMesh("windows")
    private val roofMesh = cityTriangulation.rf().toCustomMesh("roof")
    private val crossroadsMesh = roads.crossroads.toCustomMesh("cross")
    private val roadsMesh = roads.roads.toCustomMesh("roadz")

    fun render(fc: FrameContext) = with(fc) {
        Renderable(
            standart {
                baseColor = White
                baseColorTexture = texture("city/dw.jpg")
                set("windowTexture", texture("city/lw.jpg"))
                pbr.metallic = 0.6f
            },
            plugin("texture", "city/window.texture.plugin.frag"),
            mesh = windowsMesh
        )

        Renderable(
            standart {
                baseColorTexture = texture("city/roof.jpg")
                pbr.metallic = 0.6f
            },
            mesh = roofMesh,
        )

        Renderable(
            standart {
                baseColor = white(0.1f)
                pbr.metallic = 0.0f
                pbr.roughness = 0.8f
            },
            mesh = cube(),
            transform = scale(800f, 1f, 800f).translate(-0.501f.y)
        )

        Renderable(
            standart {
                baseColor = White
                baseColorTexture = texture("city/crossroad.jpg")
                pbr.metallic = 0.0f
                pbr.roughness = 0.8f
            },
            mesh = crossroadsMesh
        )

        Renderable(
            standart {
                baseColor = White
                baseColorTexture = texture("city/road.jpg")
                pbr.metallic = 0.0f
                pbr.roughness = 0.8f
            },
            mesh = roadsMesh
        )


        Sky(starrySky {
            colorness = 0.4f
            density = 30f
            size = 20f
        })


        Filter(fxaa())
        // Filter(fragment("city/shadow-debug.frag"))

        Gui {
            Filler()
            Text(
                id = "fps",
                fontResource = "ubuntu.ttf",
                height = 20,
                text = "FPS ${frameInfo.avgFps.toInt()}",
                color = Color(0xFF66FF55)
            )
        }
    }
}