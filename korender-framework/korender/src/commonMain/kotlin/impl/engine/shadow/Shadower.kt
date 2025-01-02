package com.zakgof.korender.impl.engine.shadow

import com.zakgof.korender.impl.engine.RenderContext
import com.zakgof.korender.impl.engine.Renderable

internal interface Shadower {

    val cascadeNumber: Int

    fun render(
        renderContext: RenderContext,
        shadowCasters: List<Renderable>,
        fixer: (Any?) -> Any?
    ): Map<String, Any?>
}