package com.zakgof.korender.impl.geometry

import com.zakgof.korender.Attributes.INSTPOS
import com.zakgof.korender.Attributes.INSTROT
import com.zakgof.korender.Attributes.INSTSCALE
import com.zakgof.korender.Attributes.INSTSCREEN
import com.zakgof.korender.Attributes.INSTTEX
import com.zakgof.korender.Attributes.MODEL0
import com.zakgof.korender.Attributes.MODEL1
import com.zakgof.korender.Attributes.MODEL2
import com.zakgof.korender.Attributes.MODEL3
import com.zakgof.korender.Attributes.WEIGHTS
import com.zakgof.korender.IndexType
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.MeshInitializer
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.impl.buffer.put
import com.zakgof.korender.impl.camera.Camera
import com.zakgof.korender.impl.engine.BillboardInstance
import com.zakgof.korender.impl.engine.ElementDeclaration
import com.zakgof.korender.impl.engine.Inventory
import com.zakgof.korender.impl.engine.MeshInstance
import com.zakgof.korender.impl.engine.Retentionable
import com.zakgof.korender.impl.font.Font
import com.zakgof.korender.impl.material.TextureLinkDeclaration

internal interface InternalMeshDeclaration : MeshDeclaration, Retentionable

internal interface Instanceable {

    val count: Int

    fun instancing(
        meshLink: MeshLink,
        reverseZ: Boolean,
        camera: Camera?,
        inventory: Inventory,
        addUniforms: MutableMap<String, Any?>,
        addDefs: MutableSet<String>
    )
}

internal data class Cube(val halfSide: Float, override val retentionPolicy: RetentionPolicy) : InternalMeshDeclaration
internal data class DecalCube(val halfSide: Float = 0.5f, override val retentionPolicy: RetentionPolicy) : InternalMeshDeclaration
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
    override val count: Int,
    val mesh: MeshDeclaration,
    val static: Boolean,
    val transparent: Boolean,
    override val retentionPolicy: RetentionPolicy,
    val instancer: () -> List<MeshInstance>
) : InternalMeshDeclaration, Instanceable {

    override fun equals(other: Any?): Boolean = (other is InstancedMesh && other.id == id)

    override fun hashCode(): Int = id.hashCode()

    override fun instancing(meshLink: MeshLink, reverseZ: Boolean, camera: Camera?, inventory: Inventory, addUniforms: MutableMap<String, Any?>, addDefs: MutableSet<String>) {
        val cpuMesh = meshLink.cpuMesh
        if (!static || !cpuMesh.instancesInitialized || transparent) {
            var instances = instancer()
            val sortFactor = if (reverseZ) -1f else 1f
            if (transparent) {
                instances = instances.sortedBy { (camera!!.mat4 * it.transform.offset()).z * sortFactor }
            }
            cpuMesh.updateMesh {
                instances.forEachIndexed { i, it ->
                    val m = it.transform.mat4
                    attrSet(MODEL0, i, floatArrayOf(m.m00, m.m10, m.m20, m.m30))
                    attrSet(MODEL1, i, floatArrayOf(m.m01, m.m11, m.m21, m.m31))
                    attrSet(MODEL2, i, floatArrayOf(m.m02, m.m12, m.m22, m.m32))
                    attrSet(MODEL3, i, floatArrayOf(m.m03, m.m13, m.m23, m.m33))
                }
            }
            cpuMesh.instancesInitialized = true
            meshLink.updateGpu(instances.size, true)

            if (cpuMesh.attrMap.containsKey(WEIGHTS)) {
                val texDecl = TextureLinkDeclaration(id, 32 * 4, cpuMesh.instanceCount, retentionPolicy)
                inventory.textureLink(texDecl)?.let { jointTextureLink ->
                    instances.forEachIndexed { i, instance ->
                        jointTextureLink.buffer.position(32 * 4 * 4 * i)
                        instance.jointMatrices!!.forEach { jm ->
                            jointTextureLink.buffer.put(jm.asArray())
                        }
                    }
                    jointTextureLink.uploadData()
                    addUniforms["jntTexture"] = jointTextureLink.texture
                } ?: return
            }

        }
        addDefs += "INSTANCING"
    }
}

internal data class InstancedBillboard(
    val id: String,
    override val count: Int,
    val static: Boolean,
    val transparent: Boolean,
    override val retentionPolicy: RetentionPolicy,
    val instancer: () -> List<BillboardInstance>
) : InternalMeshDeclaration, Instanceable {
    override fun equals(other: Any?): Boolean = (other is InstancedBillboard && other.id == id)
    override fun hashCode(): Int = id.hashCode()

    override fun instancing(meshLink: MeshLink, reverseZ: Boolean, camera: Camera?, inventory: Inventory, addUniforms: MutableMap<String, Any?>, addDefs: MutableSet<String>) {
        val cpuMesh = meshLink.cpuMesh
        if (!static || !cpuMesh.instancesInitialized || transparent) {
            var instances = instancer()
            val sortFactor = if (reverseZ) -1f else 1f
            if (transparent) {
                instances = instances.sortedBy { (camera!!.mat4 * it.pos).z * sortFactor }
            }
            cpuMesh.updateMesh {
                instances.forEachIndexed { i, it ->
                    attrSet(INSTPOS, i, it.pos)
                    attrSet(INSTROT, i, it.phi)
                    attrSet(INSTSCALE, i, it.scale)
                }
            }
            meshLink.updateGpu(instances.size, true)
            cpuMesh.instancesInitialized = true
        }
        addDefs += "INSTANCING"
    }
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
) : InternalMeshDeclaration, Instanceable {
    override fun equals(other: Any?): Boolean = (other is CustomMesh && other.id == id)
    override fun hashCode(): Int = id.hashCode()

    override val count = -1

    override fun instancing(meshLink: MeshLink, reverseZ: Boolean, camera: Camera?, inventory: Inventory, addUniforms: MutableMap<String, Any?>, addDefs: MutableSet<String>) {
        if (dynamic) {
            meshLink.cpuMesh.updateMesh(block)
            meshLink.updateGpu(0, false)
        }
    }
}

internal class FontMesh(
    val id: String,
    override val count: Int,
    val declaration: ElementDeclaration.Text,
    val width: Float,
    val height: Float,
    val xoffset: Float,
    val yoffset: Float,
    val font: Font
) : InternalMeshDeclaration, Instanceable {
    override fun equals(other: Any?): Boolean =
        (other is FontMesh && other.id == id)

    override fun hashCode(): Int = id.hashCode()

    override val retentionPolicy = declaration.retentionPolicy

    override fun instancing(meshLink: MeshLink, reverseZ: Boolean, camera: Camera?, inventory: Inventory, addUniforms: MutableMap<String, Any?>, addDefs: MutableSet<String>) {
        val mesh = meshLink.cpuMesh
        if (!declaration.static || !mesh.instancesInitialized) {
            mesh.updateMesh {
                val h = declaration.height.toFloat() / height
                val aspect = height.toFloat() / width.toFloat()
                val x = xoffset / width
                val y = 1.0f - yoffset / height
                var xx = x
                for (i in declaration.text.indices) {
                    val c = declaration.text[i].code
                    val ratio = font.widths[c]
                    val width = h * ratio * aspect
                    attrSet(INSTTEX, i, floatArrayOf((c % 16) / 16.0f, (c / 16) / 16.0f, ratio / 16f, 1 / 16f))
                    attrSet(INSTSCREEN, i, floatArrayOf(xx, y, width, -h))
                    xx += width
                }
            }
            mesh.instancesInitialized = true
            meshLink.updateGpu(declaration.text.length, true)
        }
    }
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