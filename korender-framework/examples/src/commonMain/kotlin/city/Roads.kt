package com.zakgof.korender.examples.city

import com.zakgof.korender.examples.city.controller.land
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y

class Roads(val roads: Triangulation, val crossroads: Triangulation, val fillers: Triangulation)

private class RoadTriangulation(
    override val points: MutableList<Vec3> = mutableListOf(),
    override val normals: MutableList<Vec3> = mutableListOf(),
    override val texs: MutableList<Vec2> = mutableListOf(),
    override val indexes: MutableList<Int> = mutableListOf()
) : Triangulation {

    var idx = 0

    fun box(heightField: (Float, Float) -> Float, xx: Int, zz: Int, width: Int, height: Int, texScale: Int, rota: Boolean) {

        points += heightField.land(xx.toFloat(), zz.toFloat())
        points += heightField.land((xx + width).toFloat(), zz.toFloat())
        points += heightField.land((xx + width).toFloat(), (zz + height).toFloat())
        points += heightField.land(xx.toFloat(), (zz + height).toFloat())
        normals += List(4) { 1.y }
        if (!rota) {
            texs += Vec2(0f, 0f)
            texs += Vec2((width / texScale).toFloat(), 0f)
            texs += Vec2((width / texScale).toFloat(), (height / texScale).toFloat())
            texs += Vec2(0f, (height / texScale).toFloat())
        } else {
            texs += Vec2(0f, 0f)
            texs += Vec2(0f, (width / texScale).toFloat())
            texs += Vec2((height / texScale).toFloat(), (width / texScale).toFloat())
            texs += Vec2((height / texScale).toFloat(), 0f)
        }
        indexes += listOf(0, 2, 1, 0, 3, 2).map { idx + it }
        idx += 4
    }
}

fun roads(heightField: (Float, Float) -> Float): Roads {
    val crossroads = RoadTriangulation()
    for (xx in 0..16) {
        for (zz in 0..16) {
            crossroads.box(heightField, -192 + xx * 24 + 3, -192 + zz * 24 + 3, 2, 2, 2, false)
        }
    }
    val roads = RoadTriangulation()
    for (xx in 0 until 16) {
        for (zz in 0..16) {
            roads.box(heightField, -192 + xx * 24 + 5, -192 + zz * 24 + 3, 22, 2, 2, true)
        }
    }
    for (zz in 0 until 16) {
        for (xx in 0..16) {
            roads.box(heightField, -192 + xx * 24 + 3, -192 + zz * 24 + 5, 2, 22, 2, false)
        }
    }
    val fillers = RoadTriangulation()
    for (xx in 0 until 16) {
        for (zz in 0 until 16) {
            fillers.box(heightField, -192 + xx * 24 + 5, -192 + zz * 24 + 5, 22, 22, 2, false)
        }
    }
    return Roads(roads, crossroads, fillers)
}