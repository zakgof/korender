package com.zakgof.korender

import com.zakgof.korender.glgpu.Attribute

object Attributes {
    val POS: Attribute = Attribute("pos", 3)
    val NORMAL: Attribute = Attribute("normal", 3)
    val TANGENT: Attribute = Attribute("tangent", 3)
    val TEX: Attribute = Attribute("tex", 2)
    val IDX: Attribute = Attribute("idx", 1)
}