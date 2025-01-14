package city

import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y

class Roads(val roads: Triangulation, val crossroads: Triangulation)

private class RoadTriangulation(
    override val points: MutableList<Vec3> = mutableListOf(),
    override val normals: MutableList<Vec3> = mutableListOf(),
    override val texs: MutableList<Vec2> = mutableListOf(),
    override val indexes: MutableList<Int> = mutableListOf()
) : Triangulation {

    var idx = 0

    fun box(xx: Int, zz: Int, width: Int, height: Int, texScale: Int, rota: Boolean) {
        points += Vec3(xx.toFloat(), 0f, zz.toFloat())
        points += Vec3((xx + width).toFloat(), 0f, zz.toFloat())
        points += Vec3((xx + width).toFloat(), 0f, (zz + height).toFloat())
        points += Vec3(xx.toFloat(), 0f, (zz + height).toFloat())
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
        indexes += listOf(0, 2, 1, 0, 3, 2).map {idx + it}
        idx += 4
    }
}

fun roads(): Roads {
    val crossroads = RoadTriangulation()
    for (xx in 0..16) {
        for (zz in 0..16) {
            crossroads.box(-192 + xx * 24, -192 + zz * 24, 8, 8, 8, false)
        }
    }
    val roads = RoadTriangulation()
    for (xx in 0 until 16) {
        for (zz in 0 .. 16) {
            roads.box(-192 + 8 + xx * 24, -192 + zz * 24, 16, 8, 8, true)
        }
    }
    for (zz in 0 until 16) {
        for (xx in 0 .. 16) {
            roads.box(-192 + xx * 24, -192 + 8 + zz * 24, 8, 16, 8, false)
        }
    }
    return Roads(roads, crossroads)
}