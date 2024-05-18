package com.zakgof.korender.declaration

import com.zakgof.korender.impl.engine.CascadeDeclaration
import com.zakgof.korender.impl.engine.ShadowDeclaration


class ShadowContext internal constructor(private val shadowDeclaration: ShadowDeclaration) {
    fun Cascade(mapSize: Int, near: Float, far: Float) =
        shadowDeclaration.addCascade(CascadeDeclaration(mapSize, near, far))
}