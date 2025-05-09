package com.zakgof.korender.impl.context

import com.zakgof.korender.ShadowAlgorithmDeclaration
import com.zakgof.korender.context.ShadowContext
import com.zakgof.korender.impl.engine.CascadeDeclaration
import com.zakgof.korender.impl.engine.ShadowDeclaration

internal class DefaultShadowContext(private val shadowDeclaration: ShadowDeclaration) : ShadowContext {
    override fun Cascade(mapSize: Int, near: Float, far: Float, fixedYRange: Pair<Float, Float>?, algorithm: ShadowAlgorithmDeclaration) {
        shadowDeclaration.cascades += CascadeDeclaration(mapSize, near, far, fixedYRange, algorithm)
    }
}