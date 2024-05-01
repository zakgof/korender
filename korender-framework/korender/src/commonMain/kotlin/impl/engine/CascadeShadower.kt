package com.zakgof.korender.impl.engine

import com.zakgof.korender.camera.Camera
import com.zakgof.korender.declaration.UniformSupplier
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.projection.Projection

internal class CascadeShadower(private val inventory: Inventory, private val mapSize: Int, private val cascades: List<Float>, private val shadowCasters: List<Renderable>) : Shadower {

    private val shadowers: List<SingleShadower> = createShadowers()

    private fun createShadowers(): List<SingleShadower> =
        (1 until cascades.size)
            .map { SingleShadower(it - 1, inventory, mapSize, cascades[it - 1], cascades[it], shadowCasters) }

    override fun render(projection: Projection, camera: Camera, light: Vec3): UniformSupplier =
        shadowers.map { it.render(projection, camera, light) }
            .reduce { a, b -> a + b }
}
