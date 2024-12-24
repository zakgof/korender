package com.zakgof.korender.mesh

import com.zakgof.korender.context.InstancedBillboardsContext
import com.zakgof.korender.context.InstancedRenderablesContext
import com.zakgof.korender.impl.engine.MaterialDeclaration
import com.zakgof.korender.impl.geometry.Attribute
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

object Meshes {

    fun cube(halfSide: Float = 0.5f) : MeshDeclaration = Cube(halfSide)
    fun sphere(radius: Float = 1.0f) : MeshDeclaration = Sphere(radius)
    fun obj(objFile: String) : MeshDeclaration = ObjMesh(objFile)
    fun screenQuad() : MeshDeclaration = ScreenQuad

    fun customMesh(id: Any, vertexCount: Int, indexCount: Int, vararg attributes: Attribute, dynamic: Boolean = false, block: MeshInitializer.() -> Unit) : MeshDeclaration =
        CustomMesh(id, vertexCount, indexCount, attributes.asList(), dynamic, block)

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

internal data class InstancedMesh(val id: Any, val count: Int, val mesh: MeshDeclaration, val material: MaterialDeclaration, val static: Boolean, val transparent: Boolean, val block: InstancedRenderablesContext.() -> Unit) : MeshDeclaration {
    override fun equals(other: Any?): Boolean = (other is InstancedMesh && other.id == id)
    override fun hashCode(): Int = id.hashCode()
}

internal data class InstancedBillboard(val id: Any, val count: Int, val transparent: Boolean, val block: InstancedBillboardsContext.() -> Unit) : MeshDeclaration {
    override fun equals(other: Any?): Boolean = (other is InstancedBillboard && other.id == id)
    override fun hashCode(): Int = id.hashCode()
}

internal data class CustomMesh(val id: Any, val vertexCount: Int, val indexCount: Int, val attributes: List<Attribute>, val dynamic: Boolean, val block: MeshInitializer.() -> Unit) : MeshDeclaration {
    override fun equals(other: Any?): Boolean = (other is CustomMesh && other.id == id)
    override fun hashCode(): Int = id.hashCode()
}

internal data class HeightField(val id: Any, val cellsX: Int, val cellsZ: Int, val cellWidth: Float, val height: (Int, Int) -> Float) : MeshDeclaration {
    override fun equals(other: Any?): Boolean = (other is HeightField && other.id == id)
    override fun hashCode(): Int = id.hashCode()
}

interface MeshInitializer {
    fun attr(attr: Attribute, vararg v: Float) : MeshInitializer
    fun pos(vararg position: Vec3) : MeshInitializer
    fun pos(vararg v: Float) : MeshInitializer
    fun normal(vararg position: Vec3) : MeshInitializer
    fun normal(vararg v: Float) : MeshInitializer
    fun tex(vararg position: Vec2) : MeshInitializer
    fun tex(vararg v: Float) : MeshInitializer
    fun scale(vararg position: Vec2) : MeshInitializer
    fun scale(vararg v: Float) : MeshInitializer
    fun phi(vararg v: Float) : MeshInitializer
    fun index(vararg indices: Int) : MeshInitializer
}