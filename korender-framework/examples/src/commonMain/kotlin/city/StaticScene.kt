package com.zakgof.korender.examples.city

import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.examples.city.controller.Controller
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Color.Companion.White


class StaticScene(private val kc: KorenderContext, private val controller: Controller) {

    private val cityTriangulation = cityTriangulation()
    private val roads = roads(controller.heightField)

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
    private val fillersMesh = roads.fillers.toCustomMesh("fillers")



    fun render(fc: FrameContext) = with(fc) {

        val roof = standart {
            baseColorTexture = texture("city/roof.jpg")
            pbr.metallic = 0.6f
        }
        val windows = standart {
            baseColorTexture = texture("city/dw.jpg")
            set("windowTexture", texture("city/lw.jpg", wrap = TextureWrap.MirroredRepeat))
            pbr.metallic = 0.3f
        }
        val asphalt = standart {
            baseColorTexture = texture("texture/asphalt-albedo.jpg")
            pbr.metallic = 0.0f
            pbr.roughness = 0.8f
            triplanarScale = 1.0f
        }
        val crossroad = standart {
            baseColor = White
            baseColorTexture = texture("city/crossroad.jpg")
            pbr.metallic = 0.0f
            pbr.roughness = 0.8f
        }
        val road = standart {
            baseColor = White
            baseColorTexture = texture("city/road.jpg")
            pbr.metallic = 0.0f
            pbr.roughness = 0.8f
        }

        Renderable(
            windows,
            plugin("emission", "city/window.emission.plugin.frag"),
            mesh = windowsMesh
        )

        Renderable(roof, mesh = roofMesh)

        Renderable(asphalt, mesh = fillersMesh)

//        Renderable(asphalt,
//            mesh = cube(),
//            transform = scale(386f, 2f, 386f).translate(-383f, 1f, -383f)
//        )

        Renderable(
            crossroad,
            mesh = crossroadsMesh
        )

        Renderable(
            road,
            mesh = roadsMesh
        )

        val sky = starrySky {
            colorness = 0.4f
            density = 20f
            size = 20f
            set("moonTexture", texture("city/moon.png"))
        }
        val moon = plugin("secsky", "city/moon.secsky.plugin.frag")

        Sky(sky, moon)

//        Scene(gltfResource = "city/racecar.glb", transform = scale(0.6f).translate(3.2f, 0.11f, -98f))
//        Scene(gltfResource = "city/car2.glb", transform = scale(0.2f).translate(6.2f, 0.00f, -98f))

        Filter(water(), sky, moon)
        Filter(fog())

        Gui {
            Filler()
            Text(
                id = "fps",
                text = "FPS ${frameInfo.avgFps.toInt()}",
                fontResource = "font/anta.ttf",
                height = 20,
                color = Color(0xFF66FF55)
            )
        }
    }
}