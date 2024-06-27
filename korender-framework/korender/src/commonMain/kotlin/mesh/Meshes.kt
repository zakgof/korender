package com.zakgof.korender.mesh

import com.zakgof.korender.context.InstancedBillboardsContext
import com.zakgof.korender.context.InstancedRenderablesContext
import com.zakgof.korender.impl.engine.MaterialDeclaration
import com.zakgof.korender.impl.geometry.Attribute

object Meshes {

    fun cube(halfSide: Float = 0.5f) : MeshDeclaration = Cube(halfSide)
    fun sphere(radius: Float = 1.0f) : MeshDeclaration = Sphere(radius)
    fun obj(objFile: String) : MeshDeclaration = ObjMesh(objFile)
    fun screenQuad() : MeshDeclaration = ScreenQuad

    fun customMesh(id: Any, static: Boolean, vertexCount: Int, indexCount: Int, vararg attributes: Attribute, block: MeshInitializer.() -> Unit) : MeshDeclaration =
        CustomMesh(id, static, vertexCount, indexCount, attributes.asList(), block)

    fun heightField(id: Any, cellsX: Int, cellsZ: Int, cellWidth: Float, height: (Int, Int) -> Float) : MeshDeclaration =
        HeightField(id, cellsX, cellsZ, cellWidth, height)
}

sealed interface MeshDeclaration

internal data class Cube(val halfSide: Float) : MeshDeclaration
internal data class Sphere(val radius: Float) : MeshDeclaration
internal data class ObjMesh(val objFile: String) : MeshDeclaration
internal data object Billboard : MeshDeclaration
internal data object ImageQuad : MeshDeclaration
internal data object ScreenQuad : MeshDeclaration

internal data class InstancedMesh(val id: Any, val count: Int, val mesh: MeshDeclaration, val material: MaterialDeclaration, val static: Boolean, val block: InstancedRenderablesContext.() -> Unit) : MeshDeclaration {
    override fun equals(other: Any?): Boolean = (other is InstancedMesh && other.id == id)
    override fun hashCode(): Int = id.hashCode()
}

internal data class InstancedBillboard(val id: Any, val count: Int, val zSort: Boolean, val block: InstancedBillboardsContext.() -> Unit) : MeshDeclaration {
    override fun equals(other: Any?): Boolean = (other is InstancedBillboard && other.id == id)
    override fun hashCode(): Int = id.hashCode()
}

internal data class CustomMesh(val id: Any, val static: Boolean, val vertexCount: Int, val indexCount: Int, val attributes: List<Attribute>, val block: MeshInitializer.() -> Unit) : MeshDeclaration {
    override fun equals(other: Any?): Boolean = (other is CustomMesh && other.id == id)
    override fun hashCode(): Int = id.hashCode()
}

internal data class HeightField(val id: Any, val cellsX: Int, val cellsZ: Int, val cellWidth: Float, val height: (Int, Int) -> Float) : MeshDeclaration {
    override fun equals(other: Any?): Boolean = (other is HeightField && other.id == id)
    override fun hashCode(): Int = id.hashCode()
}

interface MeshInitializer {
    fun vertex(block: Vertex.() -> Unit)
    fun vertex(vertex: Vertex)
    fun vertices(vararg float: Float)
    fun indices(vararg indexes: Int)
}