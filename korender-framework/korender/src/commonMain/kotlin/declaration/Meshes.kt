package com.zakgof.korender.declaration

import com.zakgof.korender.impl.engine.BillboardInstance
import com.zakgof.korender.impl.engine.MeshInstance
import com.zakgof.korender.impl.geometry.Attribute
import com.zakgof.korender.impl.geometry.Vertex
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

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

    data class InstancedMesh(
        val id: Any,
        val count: Int,
        val mesh: MeshDeclaration,
        val material: MaterialDeclaration,
        val static: Boolean,
        val block: InstancedRenderablesContext.() -> Unit,
    ) : MeshDeclaration {
        override fun equals(other: Any?): Boolean = (other is InstancedMesh && other.id == id)
        override fun hashCode(): Int = id.hashCode()
    }

    data class InstancedBillboard(val id: Any, val count: Int, val zSort: Boolean, val block: InstancedBillboardsContext.() -> Unit) : MeshDeclaration {
        override fun equals(other: Any?): Boolean = (other is InstancedBillboard && other.id == id)
        override fun hashCode(): Int = id.hashCode()
    }
    data class Custom(val id: Any, val static: Boolean, val vertexCount: Int, val indexCount: Int, val attributes: List<Attribute>, val block: MeshInitializer.() -> Unit) : MeshDeclaration {
        override fun equals(other: Any?): Boolean = (other is Custom && other.id == id)
        override fun hashCode(): Int = id.hashCode()
    }

    data class HeightField(val id: Any, val cellsX: Int, val cellsZ: Int, val cellWidth: Float, val height: (Int, Int) -> Float) : MeshDeclaration {
        override fun equals(other: Any?): Boolean = (other is HeightField && other.id == id)
        override fun hashCode(): Int = id.hashCode()
    }
}

interface MeshInitializer {
    fun vertex(block: Vertex.() -> Unit)

    fun vertex(vertex: Vertex)

    fun vertices(vararg float: Float)
    fun indices(vararg indexes: Int)
}

// TODO: extract interfaces
class InstancedBillboardsContext internal constructor(private val instances: MutableList<BillboardInstance>) {

    fun Instance(pos: Vec3, scale: Vec2 = Vec2.ZERO, phi: Float = 0f) =
        instances.add(BillboardInstance(pos, scale, phi))
}


class InstancedRenderablesContext internal constructor(private val instances: MutableList<MeshInstance>) {

    fun Instance(transform: Transform) =
        instances.add(MeshInstance(transform))
}