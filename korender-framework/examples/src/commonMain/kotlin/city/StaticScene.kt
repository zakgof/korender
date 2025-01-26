package com.zakgof.korender.examples.city

import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.examples.city.controller.Controller
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Color.Companion.White
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Transform.Companion.translate


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
                baseColorTexture = texture("texture/asphalt-albedo.jpg")
                pbr.metallic = 0.0f
                pbr.roughness = 0.8f
                triplanarScale = 1.0f
            },
            mesh = heightField("hf", 128, 128, 3.0f) { xx, zz ->
                controller.heightField(-192f + xx * 3.0f, -192f + zz * 3.0f)
            },
            transform = translate(0f, -0.02f, 0f)
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


        val sky = starrySky {
            colorness = 0.4f
            density = 30f
            size = 20f
        }
        Sky(sky)

        Scene(gltfResource = "city/racecar.glb", transform = scale(0.6f).translate(3.2f, 0.11f, -98f))

        Scene(gltfResource = "city/car2.glb", transform = scale(0.2f).translate(6.2f, 0.00f, -98f))


        Filter(water(), sky)

        Gui {
            Filler()
            Text(
                id = "fps",
                fontResource = "font/anta.ttf",
                height = 20,
                text = "FPS ${frameInfo.avgFps.toInt()}",
                color = Color(0xFF66FF55)
            )
        }
    }
}