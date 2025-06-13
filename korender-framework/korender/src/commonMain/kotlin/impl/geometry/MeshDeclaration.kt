package com.zakgof.korender.impl.geometry

import com.zakgof.korender.IndexType
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.MeshInitializer
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.impl.engine.BillboardInstance
import com.zakgof.korender.impl.engine.MeshInstance
import impl.engine.Retentionable

internal interface InternalMeshDeclaration : MeshDeclaration, Retentionable

internal data class Cube(val halfSide: Float, override val retentionPolicy: RetentionPolicy) : InternalMeshDeclaration
internal data class Sphere(val radius: Float, val slices: Int, val sectors: Int, override val retentionPolicy: RetentionPolicy) : InternalMeshDeclaration
internal data class ObjMesh(val objFile: String, override val retentionPolicy: RetentionPolicy) : InternalMeshDeclaration
internal data class Billboard(override val retentionPolicy: RetentionPolicy) : InternalMeshDeclaration
internal data class ImageQuad(override val retentionPolicy: RetentionPolicy) : InternalMeshDeclaration
internal data class ScreenQuad(override val retentionPolicy: RetentionPolicy) : InternalMeshDeclaration
internal data class Quad(val halfSideX: Float, val halfSideY: Float, override val retentionPolicy: RetentionPolicy) : InternalMeshDeclaration
internal data class CylinderSide(val height: Float, val radius: Float, val sectors: Int, override val retentionPolicy: RetentionPolicy) : InternalMeshDeclaration
internal data class ConeTop(val height: Float, val radius: Float, val sectors: Int, override val retentionPolicy: RetentionPolicy) : InternalMeshDeclaration
internal data class Disk(val radius: Float, val sectors: Int, override val retentionPolicy: RetentionPolicy) : InternalMeshDeclaration


internal data class InstancedMesh(
    val id: String,
    val count: Int,
    val mesh: MeshDeclaration,
    val static: Boolean,
    val transparent: Boolean,
    override val retentionPolicy: RetentionPolicy,
    val instancer: () -> List<MeshInstance>
) : InternalMeshDeclaration {
    override fun equals(other: Any?): Boolean = (other is InstancedMesh && other.id == id)
    override fun hashCode(): Int = id.hashCode()
}

internal data class InstancedBillboard(
    val id: String,
    val count: Int,
    val static: Boolean,
    val transparent: Boolean,
    override val retentionPolicy: RetentionPolicy,
    val instancer: () -> List<BillboardInstance>
) : InternalMeshDeclaration {
    override fun equals(other: Any?): Boolean = (other is InstancedBillboard && other.id == id)
    override fun hashCode(): Int = id.hashCode()
}

internal data class CustomMesh(
    val id: String,
    val vertexCount: Int,
    val indexCount: Int,
    val attributes: List<MeshAttribute<*>>,
    val dynamic: Boolean,
    val indexType: IndexType?,
    override val retentionPolicy: RetentionPolicy,
    val block: MeshInitializer.() -> Unit
) : InternalMeshDeclaration {
    override fun equals(other: Any?): Boolean = (other is CustomMesh && other.id == id)
    override fun hashCode(): Int = id.hashCode()
}

internal data class CustomCpuMesh(
    val id: String,
    val mesh: CMesh,
    override val retentionPolicy: RetentionPolicy
) : InternalMeshDeclaration {
    override fun equals(other: Any?): Boolean = (other is CustomCpuMesh && other.id == id)
    override fun hashCode(): Int = id.hashCode()
}

internal data class HeightField(
    val id: String,
    val cellsX: Int,
    val cellsZ: Int,
    val cellWidth: Float,
    val height: (Int, Int) -> Float,
    override val retentionPolicy: RetentionPolicy
) : InternalMeshDeclaration {
    override fun equals(other: Any?): Boolean = (other is HeightField && other.id == id)
    override fun hashCode(): Int = id.hashCode()
}