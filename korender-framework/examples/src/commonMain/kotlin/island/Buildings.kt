package com.zakgof.korender.examples.island

import com.zakgof.korender.ShaderPlugin
import com.zakgof.korender.ShaderPluginId
import com.zakgof.korender.scope.FrameScope
import com.zakgof.korender.scope.KorenderScope
import com.zakgof.korender.examples.island.city.CityGenerator
import com.zakgof.korender.examples.island.city.generateBuilding
import com.zakgof.korender.math.ColorRGBA.Companion.white
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3

private lateinit var buildingsAlbedoPlugin: ShaderPlugin
private lateinit var buildingsMetallicPlugin: ShaderPlugin

fun KorenderScope.loadBuildings(bytes: ByteArray): CityGenerator {
    buildingsAlbedoPlugin = shaderPlugin(ShaderPluginId.ALBEDO, "island/building/shader/island.window.albedo.frag")
    buildingsMetallicPlugin = shaderPlugin(ShaderPluginId.METALLIC_ROUGHNESS, "island/building/shader/island.window.metallic.frag")
    return loadBinary(bytes) {
        val cityGenerator = CityGenerator(this@loadBuildings)
        val size = bytes.size / (2 * 3 * 4)
        (0 until size).forEach { i ->
            val p1 = getVec3()
            val p2 = getVec3()

            val c = (p1 + p2) * 0.5f * 512f
            val halfDim = (p2 - p1) * 0.3f * 512f

            val xoffset = (c - halfDim).x.toInt()
            val yoffset = (c - halfDim).z.toInt()
            val xsize = (halfDim.x * 2f).toInt()
            val ysize = (halfDim.z * 2f).toInt()
            generateBuilding(cityGenerator, xoffset, yoffset, xsize, ysize, i)
        }
        cityGenerator
    }
}

fun FrameScope.buildings(cityGenerator: CityGenerator) {

    val dim = 32f * 512f

    val tr = scale(32f).translate(Vec3(-dim * 0.5f, -100f, -dim * 0.5f))
    val concrete = base {
        color = white(2.0f)
        colorTexture = texture("infcity/roof.jpg")
        metallicFactor = 0f
        roughnessFactor = 1f
    }
    val wnd = base {
        color = white(2.0f)
        colorTexture = texture("infcity/roof.jpg")
        metallicFactor = 0f
        roughnessFactor = 1f
        plugin(buildingsAlbedoPlugin)
        plugin(buildingsMetallicPlugin)
    }

    Renderable(
        wnd,
        mesh = mesh("lw", cityGenerator.lightWindow),
        transform = tr
    )
    Renderable(
        concrete,
        mesh = mesh("rf", cityGenerator.roof),
        transform = tr
    )
}