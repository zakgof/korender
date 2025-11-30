package com.zakgof.korender.context

import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.PostShadingEffect
import com.zakgof.korender.math.Vec3

interface DeferredShadingContext {

    /**
     * Defines modifiers for the shading step.
     *
     * @param shadingModifiers shading material modifiers
     */
    fun Shading(vararg shadingModifiers: MaterialModifier)

    /**
     * Defines post shading effect modifiers.
     *
     * @param effects post shading effect material modifiers
     */
    fun PostShading(vararg effects: PostShadingEffect)

    /**
     * Creates a decal.
     *
     * @param materialModifiers material modifiers
     * @param position decal application position
     * @param look decal application direction
     * @param up up direction for decal application (corresponds to y axis of decal texture)
     * @param size decal quad size, in world space units
     */
    fun Decal(vararg materialModifiers: MaterialModifier, position: Vec3, look: Vec3, up: Vec3, size: Float)
}