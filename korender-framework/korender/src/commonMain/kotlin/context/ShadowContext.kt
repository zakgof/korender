package com.zakgof.korender.context

import com.zakgof.korender.ShadowAlgorithmDeclaration

interface ShadowContext {
    fun Cascade(mapSize: Int, near: Float, far: Float, fixedYRange: Pair<Float, Float>? = null, algorithm: ShadowAlgorithmDeclaration)
}