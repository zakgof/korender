package com.zakgof.korender.context

import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.PostShadingEffect
import com.zakgof.korender.math.Vec3

interface DeferredShadingContext {
    fun Shading(vararg shadingModifiers: MaterialModifier)
    fun PostShading(vararg effects: PostShadingEffect)

    fun Decal(vararg materialModifiers: MaterialModifier, position: Vec3, look: Vec3, up: Vec3, size: Float)
}