package com.zakgof.korender.mesh

import com.zakgof.korender.buffer.Floater
import com.zakgof.korender.impl.geometry.Attribute
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

object Attributes {

    fun byName(name: String) = mapping[name]

    val POS: Attribute = Attribute("pos", 3,
        { b, v -> write(b, v.pos!!) },
        { b, v -> v.pos = readVec3(b) }
    )

    val NORMAL: Attribute = Attribute("normal", 3,
        { b, v -> write(b, v.normal!!) },
        { b, v -> v.normal = readVec3(b) }
    )

    val TEX: Attribute = Attribute("tex", 2,
        { b, v -> write(b, v.tex!!) },
        { b, v -> v.tex = readVec2(b) }
    )

    val SCREEN: Attribute = Attribute("screen", 2,
        { b, v -> write(b, v.screen!!) },
        { b, v -> v.screen = readVec2(b) }
    )

    val SCALE: Attribute = Attribute("scale", 2,
        { b, v -> write(b, v.scale!!) },
        { b, v -> v.scale = readVec2(b) }
    )

    val PHI: Attribute = Attribute("phi", 1,
        { b, v -> b.put(v.phi!!) },
        { b, v -> v.phi = b.get() }
    )

    private val mapping= mapOf(
        "POS" to POS,
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