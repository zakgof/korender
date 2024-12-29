package com.zakgof.korender.mesh

import com.zakgof.korender.gl.GLConstants.GL_FLOAT
import com.zakgof.korender.gl.GLConstants.GL_UNSIGNED_BYTE
import com.zakgof.korender.impl.geometry.Attribute

object Attributes {

    fun byGltfName(name: String) = mapping[name]

    val POS: Attribute = Attribute("pos", 3,  4, GL_FLOAT,0)
    val NORMAL: Attribute = Attribute("normal", 3, 4, GL_FLOAT,1)
    val TEX: Attribute = Attribute("tex", 2, 4, GL_FLOAT, 2)
    val JOINTS: Attribute = Attribute("joints", 4, 1, GL_UNSIGNED_BYTE,3)
    val WEIGHTS: Attribute = Attribute("weights", 4, 4, GL_FLOAT,4)
    val SCREEN: Attribute = Attribute("screen", 2, 4, GL_FLOAT, 5)
    val SCALE: Attribute = Attribute("scale", 2, 4, GL_FLOAT, 6)
    val PHI: Attribute = Attribute("phi", 1, 4, GL_FLOAT, 7)

    private val mapping = mapOf(
        "POSITION" to POS,
        "NORMAL" to NORMAL,
        "TEXCOORD_0" to TEX,
        "JOINTS_0" to JOINTS,
        "WEIGHTS_0" to WEIGHTS,
    )
}