package com.zakgof.korender.impl.engine.shadow

import com.zakgof.korender.impl.engine.RenderContext
import com.zakgof.korender.impl.engine.Renderable
import com.zakgof.korender.math.Vec3

internal interface Shadower {

    val cascadeNumber: Int

    fun render(
        renderContext: RenderContext,
        lightDirection: Vec3,
        shadowCasters: List<Renderable>,
        fixer: (Any?) -> Any?
    ): Map<String, Any?>
}