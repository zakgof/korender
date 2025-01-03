package com.zakgof.korender.impl.engine.shadow

import com.zakgof.korender.impl.engine.CascadeDeclaration
import com.zakgof.korender.impl.engine.Inventory
import com.zakgof.korender.impl.engine.RenderContext
import com.zakgof.korender.impl.engine.Renderable
import com.zakgof.korender.math.Vec3

internal class CascadeShadower(
    private val inventory: Inventory,
    private val cascades: List<CascadeDeclaration>
) : Shadower {

    override val cascadeNumber = cascades.size

    private val shadowers: List<SingleShadower> = createShadowers()

    private fun createShadowers(): List<SingleShadower> =
        cascades.indices.map { SingleShadower(it, inventory, cascades[it]) }

    override fun render(
        renderContext: RenderContext,
        lightDirection: Vec3,
        shadowCasters: List<Renderable>,
        fixer: (Any?) -> Any?
    ): Map<String, Any?> =
        shadowers.map { it.render(renderContext, lightDirection, shadowCasters, fixer) }
            .flatMap { it.entries }.associate { it.key to it.value }
}
