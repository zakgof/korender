package com.zakgof.korender.impl.context

import com.zakgof.korender.DecalMaterial
import com.zakgof.korender.PostShadingEffect
import com.zakgof.korender.ShadingMaterialScope
import com.zakgof.korender.context.DeferredShadingScope
import com.zakgof.korender.impl.engine.DeferredShadingDeclaration
import com.zakgof.korender.impl.engine.InternalDecalDeclaration
import com.zakgof.korender.impl.material.InternalDecalMaterial
import com.zakgof.korender.impl.material.InternalShadingMaterial
import com.zakgof.korender.math.Vec3

internal class DefaultDeferredShadingScope(private var deferredShadingDeclaration: DeferredShadingDeclaration) : DeferredShadingScope {

    override fun Shading(block: ShadingMaterialScope.() -> Unit) {
        deferredShadingDeclaration.shadingMaterial = InternalShadingMaterial().also { block.invoke(it) }
    }

    override fun PostShading(vararg effects: PostShadingEffect) {
        deferredShadingDeclaration.postShadingEffects += effects
    }

    override fun Decal(material: DecalMaterial, position: Vec3, look: Vec3, up: Vec3, size: Float) {
        deferredShadingDeclaration.decals += InternalDecalDeclaration(position, look, up, size, material as InternalDecalMaterial)
    }

}