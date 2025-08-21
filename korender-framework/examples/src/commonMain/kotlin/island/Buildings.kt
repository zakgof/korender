package com.zakgof.korender.examples.island

import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.examples.island.city.CityGenerator
import com.zakgof.korender.examples.island.city.generateBuilding
import com.zakgof.korender.math.ColorRGBA.Companion.white
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3

fun loadBuildings(bytes: ByteArray): CityGenerator =
    loadBinary(bytes) {
        val cityGenerator = CityGenerator()
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

fun FrameContext.buildings(cityGenerator: CityGenerator) {

    val dim = 32f * 512f

    val tr = scale(32f).translate(Vec3(-dim * 0.5f, -100f, -dim * 0.5f))
    val concrete = base(color = white(2.0f), colorTexture = texture("infcity/roof.jpg"), metallicFactor = 0f, roughnessFactor = 1f)

    Renderable(
        concrete,
        plugin("albedo", "island/building/shader/island.window.albedo.frag"),
        mesh = mesh("lw", cityGenerator.lightWindow),
        transform = tr
    )
    Renderable(
        concrete,
        mesh = mesh("rf", cityGenerator.roof),
        transform = tr
    )
}