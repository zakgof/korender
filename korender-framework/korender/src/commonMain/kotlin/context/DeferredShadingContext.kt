package com.zakgof.korender.context

import com.zakgof.korender.PostShadingEffect

interface DeferredShadingContext {
    fun PostShading(vararg effects: PostShadingEffect)
}