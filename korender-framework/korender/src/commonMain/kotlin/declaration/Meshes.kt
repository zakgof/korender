package com.zakgof.korender.declaration

import com.zakgof.korender.impl.geometry.Attribute
import com.zakgof.korender.impl.geometry.Vertex

object Meshes {

    fun cube(halfSide: Float = 0.5f) = MeshDeclaration.Cube(halfSide)
    fun sphere(radius: Float = 1.0f) = MeshDeclaration.Sphere(radius)
    fun obj(objFile: String) = MeshDeclaration.Obj(objFile)
    fun screenQuad() = MeshDeclaration.ScreenQuad
    fun mesh(id: Any, static: Boolean, vertexCount: Int, indexCount: Int, vararg attributes: Attribute, block: MeshInitializer.() -> Unit) =
        MeshDeclaration.Custom(id, static, vertexCount, indexCount, attributes.asList(), block)

    fun heightField(id: Any, cellsX: Int, cellsZ: Int, cellWidth: Float, height: (Int, Int) -> Float) =
        MeshDeclaration.HeightField(id, cellsX, cellsZ, cellWidth, height)
}

sealed interface MeshDeclaration {
    data class Cube(val halfSide: Float) : MeshDeclaration
    data class Sphere(val radius: Float) : MeshDeclaration
    data class Obj(val objFile: String) : MeshDeclaration
    data object Billboard : MeshDeclaration // TODO position scale and shit
    data object ImageQuad : MeshDeclaration
    data object ScreenQuad : MeshDeclaration

    open class Ided(open val id: Any) {
        override fun equals(other: Any?): Boolean =
            other!!.javaClass == this.javaClass && (other as Ided).id == id
        override fun hashCode(): Int = id.hashCode()
    }

    data class InstancedMesh(
        override val id: Any,
        val count: Int,
        val mesh: MeshDeclaration,
        val material: MaterialDeclaration,
        val static: Boolean,
        val block: InstancedRenderablesContext.() -> Unit,
    ) : MeshDeclaration, Ided(id)


    data class InstancedBillboard(override val id: Any, val count: Int, val zSort: Boolean, val block: InstancedBillboardsContext.() -> Unit) : MeshDeclaration, Ided(id)
    data class Custom(override val id: Any, val static: Boolean, val vertexCount: Int, val indexCount: Int, val attributes: List<Attribute>, val block: MeshInitializer.() -> Unit) : MeshDeclaration, Ided(id)
    data class HeightField(override val id: Any, val cellsX: Int, val cellsZ: Int, val cellWidth: Float, val height: (Int, Int) -> Float) : MeshDeclaration, Ided(id)
}

interface MeshInitializer {
    fun vertex(block: Vertex.() -> Unit)

    fun vertex(vertex: Vertex)

    fun vertices(vararg float: Float)
    fun indices(vararg indexes: Int)
}