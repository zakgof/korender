package com.zakgof.korender.impl.prefab.scene

import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.impl.context.NodeContext
import com.zakgof.korender.impl.engine.KrSceneDeclaration
import com.zakgof.korender.impl.engine.RenderableDeclaration
import com.zakgof.korender.impl.engine.SceneDeclaration
import com.zakgof.korender.impl.geometry.MeshAttributes
import com.zakgof.korender.impl.material.InternalBaseMaterial
import com.zakgof.korender.impl.scene.SceneModel
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Transform
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray

@OptIn(ExperimentalSerializationApi::class, ExperimentalCoroutinesApi::class)
internal class KrScene(val declaration: KrSceneDeclaration) : AutoCloseable {

    private val prefix = "scene[${declaration.resource}]"
    private val texturePrefix = "$prefix.texture."

    private val sceneModelDeferred = declaration.nodeContext.load(declaration.resource) {
        Cbor.decodeFromByteArray<SceneModel>(it)
    }

    private suspend fun loadResource(resource: String, parent: ResourceLoader): ByteArray =
        if (resource.startsWith(texturePrefix)) {
            val sceneModel = sceneModelDeferred.await()
            sceneModel.textures[resource.substring(texturePrefix.length)]!!.bytes
        } else {
            parent(resource)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun build(sceneDeclaration: SceneDeclaration) {
        if (sceneModelDeferred.isCompleted) {
            val sceneModel = sceneModelDeferred.getCompleted()
            val childNodeContext = NodeContext(
                { loadResource(it, declaration.nodeContext.resourceLoader) },
                declaration.nodeContext.transform,
                declaration.nodeContext.retentionPolicy,
                declaration.nodeContext.time
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
        }
    }

    private fun NodeContext.material(material: SceneModel.Material) =
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

    private fun NodeContext.mesh(id: String, mesh: SceneModel.Mesh) =
        customMesh(
            id = "$prefix.mesh.$id",
            vertexCount = mesh.vertices,
            indexCount = mesh.indices,
            attributes = attributes(mesh.attrBytes.keys),
            dynamic = false
        ) {
            mesh.attrBytes.forEach {
                attrBytes(attribute(it.key), it.value)
            }
            mesh.indexBytes?.let {
                indexBytes(it)
            }
        }

    private fun attributes(attributes: Set<SceneModel.Attribute>) =
        attributes.map { attribute(it) }.toTypedArray()

    private fun attribute(attribute: SceneModel.Attribute): MeshAttribute<out Any> = when (attribute) {
        SceneModel.Attribute.POS -> MeshAttributes.POS
        SceneModel.Attribute.NORMAL -> MeshAttributes.NORMAL
        SceneModel.Attribute.TEX -> MeshAttributes.TEX
        SceneModel.Attribute.COLOR -> MeshAttributes.COLOR
        SceneModel.Attribute.COLORTEXINDEX -> MeshAttributes.COLORTEXINDEX
        SceneModel.Attribute.METALLIC -> MeshAttributes.METALLIC
        SceneModel.Attribute.ROUGHNESS -> MeshAttributes.ROUGHNESS
    }

    override fun close() {
    }
}

