package com.zakgof.korender.impl.context

import com.zakgof.korender.PostShadingEffect
import com.zakgof.korender.context.DeferredShadingContext
import com.zakgof.korender.impl.engine.DeferredShadingDeclaration

internal class DefaultDeferredShadingContext(var deferredShadingDeclaration: DeferredShadingDeclaration) : DeferredShadingContext {
    override fun PostShading(vararg effects: PostShadingEffect) {
        deferredShadingDeclaration.postShadingEffects += effects
    }
}