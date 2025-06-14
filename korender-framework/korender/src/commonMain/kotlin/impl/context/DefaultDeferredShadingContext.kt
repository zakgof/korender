package com.zakgof.korender.impl.context

import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.PostShadingEffect
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.context.DeferredShadingContext
import com.zakgof.korender.impl.engine.DeferredShadingDeclaration
import com.zakgof.korender.impl.engine.InternalDecalDeclaration
import com.zakgof.korender.math.Vec3

internal class DefaultDeferredShadingContext(private var deferredShadingDeclaration: DeferredShadingDeclaration) : DeferredShadingContext {

    override fun Shading(vararg shadingModifiers: MaterialModifier) {
        deferredShadingDeclaration.shadingModifiers += shadingModifiers
    }

    override fun PostShading(vararg effects: PostShadingEffect) {
        deferredShadingDeclaration.postShadingEffects += effects
    }

    override fun Decal(position: Vec3, look: Vec3, up: Vec3, size: Float, colorTexture: TextureDeclaration) {
        deferredShadingDeclaration.decals += InternalDecalDeclaration(position, look, up, size, colorTexture)
    }
}