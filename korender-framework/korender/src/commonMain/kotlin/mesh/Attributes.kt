package com.zakgof.korender.mesh

import com.zakgof.korender.impl.geometry.Attribute

object Attributes {

    fun byGltfName(name: String) = mapping[name]

    val POS: Attribute = Attribute("pos", 3, 0)
    val NORMAL: Attribute = Attribute("normal", 3, 1)
    val TEX: Attribute = Attribute("tex", 2, 2)
    val SCREEN: Attribute = Attribute("screen", 2, 3)
    val SCALE: Attribute = Attribute("scale", 2, 4)
    val PHI: Attribute = Attribute("phi", 1,5)

    private val mapping = mapOf(
        "POSITION" to POS,
        "NORMAL" to NORMAL,
        "TEXCOORD_0" to TEX
    )
}