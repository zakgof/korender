package com.zakgof.korender.declaration

import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

object MeshDeclarations {

    fun cube(halfSide: Float = 0.5f) = MeshDeclaration.CubeDeclaration(halfSide)
    fun sphere(radius: Float = 1.0f) = MeshDeclaration.SphereDeclaration(radius)
}

sealed interface MeshDeclaration {

    data class CubeDeclaration(val halfSide: Float) : MeshDeclaration

    data class SphereDeclaration(val radius: Float) : MeshDeclaration

    data object BillboardDeclaration : MeshDeclaration // TODO position scale and shit

    data class InstancedBillboardDeclaration(
        val id: Any,
        val count: Int
    ) : MeshDeclaration {
        lateinit var block: InstancedBillboardsContext.() -> Unit

        constructor(id: Any, count: Int, block: InstancedBillboardsContext.() -> Unit) : this(
            id,
            count
        ) {
            this.block = block
        }
    }
}

class InstancedBillboardsContext {

    val instances = mutableListOf<BillboardInstance>()

    fun Instance(pos: Vec3, scale: Vec2 = Vec2.ZERO, phi: Float = 0f) =
        instances.add(BillboardInstance(pos, scale, phi))
}

class BillboardInstance(val pos: Vec3, val scale: Vec2 = Vec2.ZERO, val phi: Float = 0f)


