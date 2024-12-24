package com.zakgof.korender.mesh

import com.zakgof.korender.buffer.Floater
import com.zakgof.korender.impl.geometry.Attribute
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

object Attributes {

    fun byGltfName(name: String) = mapping[name]!!

    val POS: Attribute = Attribute("pos", 3, 0)
    val NORMAL: Attribute = Attribute("normal", 3, 1)
    val TEX: Attribute = Attribute("tex", 2, 2)
    val SCREEN: Attribute = Attribute("screen", 2, 3)
    val SCALE: Attribute = Attribute("scale", 2, 4)
    val PHI: Attribute = Attribute("phi", 1,5)

    private val mapping = mapOf(
        "POSITION" to POS,
        "NORMAL" to NORMAL,
        "TEX" to TEX
    )
}

class Vertex(
    var pos: Vec3? = null,
    var normal: Vec3? = null,
    var tex: Vec2? = null,
    var screen: Vec2? = null,
    var scale: Vec2? = null,
    var phi: Float? = null
)

private fun readVec3(fvb: Floater): Vec3 {
    val x = fvb.get()
    val y = fvb.get()
    val z = fvb.get()
    return Vec3(x, y, z)
}

private fun readVec2(fvb: Floater): Vec2 {
    val x = fvb.get()
    val y = fvb.get()
    return Vec2(x, y)
}

private fun write(fvb: Floater, value: Vec3) {
    fvb.apply{
        put(value.x)
        put(value.y)
        put(value.z)
    }
}

private fun write(fvb: Floater, value: Vec2) {
    fvb.apply{
        put(value.x)
        put(value.y)
    }
}