package com.zakgof.korender.impl.material

import com.zakgof.korender.PostShadingEffect
import com.zakgof.korender.impl.context.NodeContext
import com.zakgof.korender.impl.engine.InternalPassDeclaration
import com.zakgof.korender.impl.engine.NodeKeeper

internal class InternalPostShadingEffect(
    val effectPasses: List<InternalPassDeclaration>,
    val keepTextures: Set<String>,
    val compositionMaterialModifier: InternalMaterialModifier,
    override val nodeContext: NodeContext
) : PostShadingEffect, NodeKeeper
