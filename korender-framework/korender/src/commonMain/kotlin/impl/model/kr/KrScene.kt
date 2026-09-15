package com.zakgof.korender.impl.model.kr

import com.zakgof.korender.IndexType
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.ModelInfo
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.impl.context.NodeContext
import com.zakgof.korender.impl.engine.ModelDeclaration
import com.zakgof.korender.impl.engine.RenderableDeclaration
import com.zakgof.korender.impl.engine.ResultKeeper
import com.zakgof.korender.impl.engine.SceneDeclaration
import com.zakgof.korender.impl.geometry.CustomMesh
import com.zakgof.korender.impl.geometry.Geometry.customMeshFromDeclaration
import com.zakgof.korender.impl.geometry.MeshAttributes
import com.zakgof.korender.impl.material.InternalBaseMaterial
import com.zakgof.korender.impl.material.InternalByteArrayTextureDeclaration
import com.zakgof.korender.impl.model.InternalModel
import com.zakgof.korender.impl.model.InternalModelInfo
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

    private var loadNotified = false

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
            val meshes = sceneModel.renderables.values.associate {
                it.meshId to childNodeContext.mesh(it.meshId, sceneModel.meshes[it.meshId]!!)
            }
            if (!loadNotified) {
                loadNotified = true
                modelDeclaration.onUpdate?.invoke(sceneModel.modelInfo(modelDeclaration.nodeContext, meshes))
            }

            sceneModel.renderables.forEach { re ->
                sceneDeclaration.append(
                    RenderableDeclaration(
                        material = childNodeContext.material(sceneModel.materials[re.value.materialId]!!),
                        mesh = meshes[re.value.meshId]!!,
                        transform = Transform(modelDeclaration.transform.mat4 * Mat4(re.value.transform)),
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
        CustomMesh(
            id = "$prefix.mesh.$id",
            vertexCount = mesh.vertices,
            indexCount = mesh.indices,
            attributes = attributes(mesh.attrBytes.keys).asList(),
            indexType = IndexType.Int,
            nodeContext = this,
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

private fun KrModel.modelInfo(nodeContext: NodeContext, meshes: Map<String, CustomMesh>): ModelInfo {
    val modelInfoTextures = textures.mapValues {
        InternalByteArrayTextureDeclaration(
            id = it.key,
            extension = it.value.format,
            fileBytesLoader = { it.value.bytes },
            filter = TextureFilter.MipMap,
            wrap = TextureWrap.Repeat,
            aniso = 1024,
            nodeContext = nodeContext
        )
    }
    val renderables = renderables.values.map { rend ->
        InternalModelInfo.Renderable(
            rend.id,
            customMeshFromDeclaration(meshes[rend.meshId]!!, -1),
            materials[rend.materialId]!!.toMaterialInfo(modelInfoTextures)
        )
    }
    val instance = InternalModelInfo.Node("kr", null, null, renderables)
    return InternalModelInfo(
        listOf(instance),
        null,
        null
    )
}

private fun KrModel.Material.toMaterialInfo(textures: Map<String, InternalByteArrayTextureDeclaration>) = InternalModelInfo.Material(
    name = this.id,
    color = ColorRGBA(this.baseColor),
    colorTextureResource = colorTextureId?.let { textures[colorTextureId]!! },
    metallicFactor = this.metallic ?: 0.1f,
    roughnessFactor = this.roughness ?: 0.5f,
    stochasticSharpness = this.stochasticSharpness,
    triplanarScale = this.triplanarScale
)

