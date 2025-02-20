package com.zakgof.korender.impl.context

import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.PostShadingEffect
import com.zakgof.korender.context.DeferredShadingContext
import com.zakgof.korender.impl.engine.DeferredShadingDeclaration

internal class DefaultDeferredShadingContext(private var deferredShadingDeclaration: DeferredShadingDeclaration) : DeferredShadingContext {

    override fun Shading(vararg materialModifiers: MaterialModifier) {
        deferredShadingDeclaration.shadingModifiers += materialModifiers
    }

    override fun PostShading(vararg effects: PostShadingEffect) {
        deferredShadingDeclaration.postShadingEffects += effects
    }
}