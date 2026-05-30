package com.zakgof.korender.impl.model.kr

import com.zakgof.korender.IndexType
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.impl.context.NodeContext
import com.zakgof.korender.impl.engine.ModelDeclaration
import com.zakgof.korender.impl.engine.RenderableDeclaration
import com.zakgof.korender.impl.engine.ResultKeeper
import com.zakgof.korender.impl.engine.SceneDeclaration
import com.zakgof.korender.impl.geometry.MeshAttributes
import com.zakgof.korender.impl.material.InternalBaseMaterial
import com.zakgof.korender.impl.model.InternalModel
import com.zakgof.korender.impl.scene.KrModel
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Transform
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray

@OptIn(ExperimentalSerializationApi::class, ExperimentalCoroutinesApi::class)
internal class KrScene(declaration: ModelDeclaration) : InternalModel {

    private val prefix = "scene[${declaration.resource}]"
    private val texturePrefix = "$prefix.texture."

    private val sceneModelDeferred = declaration.nodeContext.load(declaration.resource) {
        Cbor.decodeFromByteArray<KrModel>(it)
    }

    private suspend fun loadResource(resource: String, parent: ResourceLoader): ByteArray =
        if (resource.startsWith(texturePrefix)) {
            val sceneModel = sceneModelDeferred.await()
            sceneModel.textures[resource.substring(texturePrefix.length)]!!.bytes
        } else {
            parent(resource)
        }

    override fun build(modelDeclaration: ModelDeclaration, sceneDeclaration: SceneDeclaration, rk: ResultKeeper?) {
        if (sceneModelDeferred.isCompleted) {
            val sceneModel = sceneModelDeferred.getCompleted()
            val childNodeContext = NodeContext(
                { loadResource(it, modelDeclaration.nodeContext.resourceLoader) },
                modelDeclaration.nodeContext.transform,
                modelDeclaration.nodeContext.retentionPolicy,
                modelDeclaration.nodeContext.time
            )
            sceneModel.renderables.forEach { re ->
                sceneDeclaration.append(
                    RenderableDeclaration(
                        material = childNodeContext.material(sceneModel.materials[re.value.materialId]!!),
                        mesh = childNodeContext.mesh(re.value.meshId, sceneModel.meshes[re.value.meshId]!!),
                        transform = Transform(Mat4(re.value.transform)),
                        transparent = false, // TODO,
                        childNodeContext
                    )
                )
            }
        } else rk?.fail()
    }

    private fun NodeContext.material(material: KrModel.Material) =
        InternalBaseMaterial().apply {
            color = ColorRGBA(material.baseColor)
            colorTexture = material.colorTextureId?.let {
                texture(texturePrefix + it)
            }
            colorTextures = material.colorTextureIds?.let {
                textureArray(*it.map { t -> texturePrefix + t }.toTypedArray())
            }
            stochasticSharpness = material.stochasticSharpness
            triplanarScale = material.triplanarScale
            metallicFactor = material.metallic ?: metallicFactor
            roughnessFactor = material.roughness ?: roughnessFactor
        }

    private fun NodeContext.mesh(id: String, mesh: KrModel.Mesh) =
        customMesh(
            id = "$prefix.mesh.$id",
            vertexCount = mesh.vertices,
            indexCount = mesh.indices,
            attributes = attributes(mesh.attrBytes.keys),
            indexType = IndexType.Int,
            dynamic = false
        ) {
            mesh.attrBytes.forEach {
                attrBytes(attribute(it.key), it.value)
            }
            mesh.indexBytes?.let {
                indexBytes(it)
            }
        }

    private fun attributes(attributes: Set<KrModel.Attribute>) =
        attributes.map { attribute(it) }.toTypedArray()

    private fun attribute(attribute: KrModel.Attribute): MeshAttribute<out Any> = when (attribute) {
        KrModel.Attribute.POS -> MeshAttributes.POS
        KrModel.Attribute.NORMAL -> MeshAttributes.NORMAL
        KrModel.Attribute.TEX -> MeshAttributes.TEX
        KrModel.Attribute.COLOR -> MeshAttributes.COLOR
        KrModel.Attribute.COLORTEXINDEX -> MeshAttributes.COLORTEXINDEX
        KrModel.Attribute.METALLIC -> MeshAttributes.METALLIC
        KrModel.Attribute.ROUGHNESS -> MeshAttributes.ROUGHNESS
    }

    override fun close() {
    }
}

