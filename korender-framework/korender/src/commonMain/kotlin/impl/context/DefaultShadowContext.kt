package com.zakgof.korender.impl.context

import com.zakgof.korender.context.ShadowContext
import com.zakgof.korender.impl.engine.CascadeDeclaration
import com.zakgof.korender.impl.engine.ShadowDeclaration

internal class DefaultShadowContext(private val shadowDeclaration: ShadowDeclaration) : ShadowContext {
    override fun Cascade(mapSize: Int, near: Float, far: Float, reservedDepth: Float) {
        shadowDeclaration.cascades += CascadeDeclaration(mapSize, near, far, reservedDepth)
    }
}