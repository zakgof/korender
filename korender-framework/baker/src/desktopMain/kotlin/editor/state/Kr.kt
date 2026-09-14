@file:OptIn(ExperimentalUuidApi::class, ExperimentalSerializationApi::class)

package com.zakgof.korender.baker.editor.state

import com.zakgof.korender.ByteArrayTextureDeclaration
import com.zakgof.korender.KorenderException
import com.zakgof.korender.Mesh
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.ModelInfo
import com.zakgof.korender.ResourceTextureDeclaration
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.impl.scene.KrModel
import com.zakgof.korender.impl.scene.KrModel.Attribute
import com.zakgof.korender.math.Mat4
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.encodeToByteArray
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


fun ModelInfo.toKrFileBytes(baseFile: File): ByteArray {

    fun renderables(mat4: Mat4, node: ModelInfo.Node): List<Pair<ModelInfo.Renderable, Mat4>> {
        val m = mat4 * (node.transform?.mat4 ?: Mat4.IDENTITY)
        return (node.renderables?.map { it to m } ?: listOf()) +
                (node.children?.flatMap { renderables(m, it) } ?: listOf())
    }

    val allRenderables = this.instances.flatMap { renderables(Mat4.IDENTITY, it) }
    val allMaterials = allRenderables.mapNotNull { it.first.material }
    val hasDefaultMaterial = allRenderables.any { it.first.material == null }
    val textureMapping = allMaterials.flatMap {
        listOfNotNull(
            it.colorTextureResource, it.normalTextureResource, it.metallicRoughnessTextureResource, it.emissionTextureResource, it.occlusionTextureResource
        )
    }.distinct().associateWith { it.toKrTexture(baseFile) }

    val materialMapping = allMaterials.distinct()
        .associateWith { it.toKtMaterial(textureMapping) }

    val allMeshes = allRenderables.map { it.first.mesh } // TODO: implement hashCode/equals for all Mesh implementations
    val meshMapping = allMeshes.distinct().associateWith { it.toKrMesh() }

    val krMaterials = materialMapping.values.associateBy { it.id }

    val krModel = KrModel(
        textures = textureMapping.values.associateBy { it.id },
        materials = if (hasDefaultMaterial) krMaterials + mapOf("default" to KrModel.Material("default")) else krMaterials,
        meshes = meshMapping.values.associateBy { it.id },
        renderables = allRenderables.map { it.first.toKrRenderable(it.second, meshMapping, materialMapping) }.associateBy { it.id }
    )

    return Cbor.encodeToByteArray(krModel)
}

private fun TextureDeclaration.toKrTexture(baseFile: File): KrModel.Texture =
    when (this) {
        is ResourceTextureDeclaration -> {
            val file = File(baseFile.parentFile,this.textureResource)
            KrModel.Texture("file:" + file.name, file.extension, file.readBytes())
        }

        is ByteArrayTextureDeclaration -> {
            KrModel.Texture(Uuid.generateV7().toString(), this.extension, this.fileBytesLoader())
        }

        else -> throw KorenderException("Unknown texture declaration type: " + this.javaClass)
    }

private fun ModelInfo.Material.toKtMaterial(textureMapping: Map<TextureDeclaration, KrModel.Texture>): KrModel.Material =
    KrModel.Material(
        id = Uuid.generateV7().toString(),
        baseColor = this.color.toLong(),
        colorTextureId = this.colorTextureResource?.let { textureMapping[it]!!.id },
        stochasticSharpness = this.stochasticSharpness,
        triplanarScale = this.triplanarScale,
        metallic = this.metallicFactor,
        roughness = this.roughnessFactor
    )

private fun Mesh.toKrMesh(): KrModel.Mesh =
    KrModel.Mesh(
        id = Uuid.generateV7().toString(),
        vertices = this.vertices.size,
        indices = this.indices?.size ?: 0,
        attrBytes = this.attributes
            .mapNotNull { attr -> attr.toKrAttr()?.let { attr to it } }
            .associate { p -> p.second to this.vertices.flatMap { vertex -> vertex.getAttr(p.first).toTypedArray().toList() }.toByteArray() },
        indexBytes = this.indices?.let { indices ->
            if (indices.size < 127) {
                indices.map { it.toByte() }.toByteArray()
            } else if (indices.size < 32767) {
                ByteArray(indices.size * 2).also { bytes ->
                    indices.forEachIndexed { i, v ->
                        bytes[i * 2] = (v and 0xFF).toByte()
                        bytes[i * 2 + 1] = ((v shr 8) and 0xFF).toByte()
                    }
                }
            } else {
                ByteArray(indices.size * 4).also { bytes ->
                    indices.forEachIndexed { i, v ->
                        bytes[i * 4] = (v and 0xFF).toByte()
                        bytes[i * 4 + 1] = ((v shr 8) and 0xFF).toByte()
                        bytes[i * 4 + 2] = ((v shr 16) and 0xFF).toByte()
                        bytes[i * 4 + 3] = ((v shr 24) and 0xFF).toByte()
                    }
                }
            }

        }
    )

private fun <T> MeshAttribute<T>.toKrAttr(): KrModel.Attribute? = when (this.name) {
    "pos" -> Attribute.POS
    "normal" -> Attribute.NORMAL
    "tex" -> Attribute.TEX
    "color" -> Attribute.COLOR
    "colortexindex" -> Attribute.COLORTEXINDEX
    "metallic" -> Attribute.METALLIC
    "roughness" -> Attribute.ROUGHNESS
    else -> null
}

private fun ModelInfo.Renderable.toKrRenderable(mat4: Mat4, meshMapping: Map<Mesh, KrModel.Mesh>, materialMapping: Map<ModelInfo.Material, KrModel.Material>) = KrModel.Renderable(
    id = Uuid.generateV7().toString(),
    meshId = meshMapping[this.mesh]!!.id,
    materialId = materialMapping[this.material]?.id ?: "default",
    transform = mat4.asArray()
)

private fun <T> Mesh.Vertex.getAttr(attr: MeshAttribute<T>): ByteArray = attr.toByteArray(this[attr]!!)