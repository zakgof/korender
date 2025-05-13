package com.zakgof.korender.impl.geometry

import com.zakgof.korender.IndexType
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.MeshInitializer
import com.zakgof.korender.context.InstancedBillboardsContext
import com.zakgof.korender.context.InstancedRenderablesContext

internal data class Cube(val halfSide: Float) : MeshDeclaration
internal data class Sphere(val radius: Float) : MeshDeclaration
internal data class ObjMesh(val objFile: String) : MeshDeclaration
internal data object Billboard : MeshDeclaration
internal data object ImageQuad : MeshDeclaration
internal data object ScreenQuad : MeshDeclaration

internal data class InstancedMesh(
    val id: Any,
    val count: Int,
    val mesh: MeshDeclaration,
    val static: Boolean,
    val transparent: Boolean,
    val block: InstancedRenderablesContext.() -> Unit
) : MeshDeclaration {
    override fun equals(other: Any?): Boolean = (other is InstancedMesh && other.id == id)
    override fun hashCode(): Int = id.hashCode()
}

internal data class InstancedBillboard(
    val id: Any,
    val count: Int,
    val transparent: Boolean,
    val block: InstancedBillboardsContext.() -> Unit
) : MeshDeclaration {
    override fun equals(other: Any?): Boolean = (other is InstancedBillboard && other.id == id)
    override fun hashCode(): Int = id.hashCode()
}

internal data class CustomMesh(
    val id: Any,
    val vertexCount: Int,
    val indexCount: Int,
    val attributes: List<MeshAttribute<*>>,
    val dynamic: Boolean,
    val indexType: IndexType?,
    val block: MeshInitializer.() -> Unit
) : MeshDeclaration {
    override fun equals(other: Any?): Boolean = (other is CustomMesh && other.id == id)
    override fun hashCode(): Int = id.hashCode()
}

internal data class HeightField(
    val id: Any,
    val cellsX: Int,
    val cellsZ: Int,
    val cellWidth: Float,
    val height: (Int, Int) -> Float
) : MeshDeclaration {
    override fun equals(other: Any?): Boolean = (other is HeightField && other.id == id)
    override fun hashCode(): Int = id.hashCode()
}