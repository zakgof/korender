package com.zakgof.korender.impl.prefab.scene

import com.zakgof.korender.BaseMaterialScope
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.context.FrameScope
import com.zakgof.korender.impl.context.DefaultFrameScope
import com.zakgof.korender.impl.context.NodeContext
import com.zakgof.korender.impl.geometry.MeshAttributes
import com.zakgof.korender.impl.prefab.InternalPrefab
import com.zakgof.korender.impl.scene.SceneModel
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Transform
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray

@OptIn(ExperimentalSerializationApi::class, ExperimentalCoroutinesApi::class)
internal class ScenePrefab(nodeContext: NodeContext, resource: String) : InternalPrefab<BaseMaterialScope> {

    private val prefix = "scene[$resource]"
    private val texturePrefix = "$prefix.texture."

    private val sceneModelDeferred = nodeContext.load(resource) {
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
    override fun render(fc: DefaultFrameScope, block: BaseMaterialScope.() -> Unit) = with(fc) {
        if (sceneModelDeferred.isCompleted) {
            val sceneModel = sceneModelDeferred.getCompleted()
            Node(resourceLoader = { loadResource(it, nodeContext.resourceLoader) }) {
                sceneModel.renderables.forEach { re ->
                    Renderable(
                        material = material(sceneModel.materials[re.value.materialId]!!),
                        mesh = mesh(re.value.meshId, sceneModel.meshes[re.value.meshId]!!),
                        transform = Transform(Mat4(re.value.transform))
                    )
                }
            }
        }
    }

    private fun FrameScope.material(material: SceneModel.Material) =
        base {
            color = ColorRGBA(material.baseColor)
            colorTexture = material.colorTextureId?.let {
                texture(texturePrefix + it)
            }
            colorTextures = material.colorTextureIds?.let {
                textureArray(*it.map { t -> texturePrefix + t }.toTypedArray())
            }
        }

    private fun FrameScope.mesh(id: String, mesh: SceneModel.Mesh) =
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
        SceneModel.Attribute.COLORTEXINDEX -> MeshAttributes.COLORTEXINDEX
    }
}

