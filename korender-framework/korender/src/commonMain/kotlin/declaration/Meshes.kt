package com.zakgof.korender.declaration

object Meshes {

    fun cube(halfSide: Float = 0.5f) = MeshDeclaration.CubeDeclaration(halfSide)
    fun sphere(radius: Float = 1.0f) = MeshDeclaration.SphereDeclaration(radius)
    fun obj(objFile: String) = MeshDeclaration.ObjDeclaration(objFile)
    fun screenQuad() = MeshDeclaration.ScreenQuadDeclaration
}

sealed interface MeshDeclaration {

    data class CubeDeclaration(val halfSide: Float) : MeshDeclaration

    data class SphereDeclaration(val radius: Float) : MeshDeclaration

    data class ObjDeclaration(val objFile: String) : MeshDeclaration

    data object BillboardDeclaration : MeshDeclaration // TODO position scale and shit
    data object ImageQuadDeclaration : MeshDeclaration

    data object ScreenQuadDeclaration : MeshDeclaration

    data class InstancedRenderableDeclaration(
        val id: Any,
        val count: Int,
        val mesh: MeshDeclaration,
        val material: MaterialDeclaration,
        val static: Boolean,
        val block: InstancedRenderablesContext.() -> Unit,
    ) : MeshDeclaration {
        override fun equals(other: Any?): Boolean =
            other is InstancedRenderableDeclaration && other.id == id

        override fun hashCode(): Int = id.hashCode()
    }


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