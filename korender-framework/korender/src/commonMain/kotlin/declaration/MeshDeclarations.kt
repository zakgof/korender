package com.zakgof.korender.declaration

import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

object MeshDeclarations {

    fun cube(halfSide: Float = 0.5f) = MeshDeclaration.CubeDeclaration(halfSide)
    fun sphere(radius: Float = 1.0f) = MeshDeclaration.SphereDeclaration(radius)
    fun obj(objFile: String) = MeshDeclaration.ObjDeclaration(objFile)
}

sealed interface MeshDeclaration {

    data class CubeDeclaration(val halfSide: Float) : MeshDeclaration

    data class SphereDeclaration(val radius: Float) : MeshDeclaration

    data class ObjDeclaration(val objFile: String) : MeshDeclaration

    data object BillboardDeclaration : MeshDeclaration // TODO position scale and shit

    data class InstancedBillboardDeclaration(
        val id: Any,
        val count: Int,
        val zSort: Boolean,
        val block: InstancedBillboardsContext.() -> Unit
    ) : MeshDeclaration {
        override fun equals(other: Any?): Boolean =
            other is InstancedBillboardDeclaration && other.id == id
        override fun hashCode(): Int = id.hashCode()
    }
}

class InstancedBillboardsContext {

    val instances = mutableListOf<BillboardInstance>()

    fun Instance(pos: Vec3, scale: Vec2 = Vec2.ZERO, phi: Float = 0f) =
        instances.add(BillboardInstance(pos, scale, phi))
}

class BillboardInstance(val pos: Vec3, val scale: Vec2 = Vec2.ZERO, val phi: Float = 0f)


