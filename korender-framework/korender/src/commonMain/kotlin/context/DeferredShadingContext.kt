package com.zakgof.korender.context

import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.PostShadingEffect

interface DeferredShadingContext {
    fun Shading(vararg materialModifiers: MaterialModifier)
    fun PostShading(vararg effects: PostShadingEffect)
}