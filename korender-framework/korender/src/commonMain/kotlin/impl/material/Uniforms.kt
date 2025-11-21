package com.zakgof.korender.impl.material

import com.zakgof.korender.PostShadingEffect
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.impl.engine.InternalPassDeclaration
import com.zakgof.korender.impl.engine.Retentionable

internal class InternalPostShadingEffect(
    val effectPasses: List<InternalPassDeclaration>,
    val keepTextures: Set<String>,
    val compositionMaterialModifier: InternalMaterialModifier,
    override val retentionPolicy: RetentionPolicy
) : PostShadingEffect, Retentionable
