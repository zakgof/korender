package com.zakgof.korender.impl.engine.shadow

import com.zakgof.korender.camera.Camera
import com.zakgof.korender.declaration.UniformSupplier
import com.zakgof.korender.impl.engine.CascadeDeclaration
import com.zakgof.korender.impl.engine.Inventory
import com.zakgof.korender.impl.engine.Renderable
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.projection.Projection

internal class CascadeShadower(private val inventory: Inventory, private val cascades: List<CascadeDeclaration>) : Shadower {

    override val cascadeNumber = cascades.size

    private val shadowers: List<SingleShadower> = createShadowers()

    private fun createShadowers(): List<SingleShadower> =
        cascades.indices.map { SingleShadower(it, inventory, cascades[it]) }

    override fun render(projection: Projection, camera: Camera, light: Vec3, shadowCasters: List<Renderable>): UniformSupplier =
        shadowers.map { it.render(projection, camera, light, shadowCasters) }
            .reduce { a, b -> a + b }
}
