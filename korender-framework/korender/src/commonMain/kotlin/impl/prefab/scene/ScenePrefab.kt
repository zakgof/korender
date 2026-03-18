package com.zakgof.korender.impl.prefab.scene

import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.impl.engine.Engine
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
internal class ScenePrefab(korenderContext: Engine.KorenderContextImpl, resource: String) : InternalPrefab {

    private val prefix = "scene[$resource]"
    private val texturePrefix = "$prefix.texture."

    private val sceneModelDeferred = korenderContext.load(resource) {
        Cbor.decodeFromByteArray<SceneModel>(it)
    }

    init {
        korenderContext.setPrefixLoader(texturePrefix) {
            sceneModelDeferred.getCompleted().textures[it]!!.bytes
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun render(fc: FrameContext, vararg materialModifiers: MaterialModifier) = with(fc) {
        if (sceneModelDeferred.isCompleted) {
            val sceneModel = sceneModelDeferred.getCompleted()
            sceneModel.renderables.forEach { re ->
                Renderable(
                    materialModifiers = materialModifiers(sceneModel.materials[re.value.materialId]!!).toTypedArray(),
                    mesh = mesh(re.value.meshId, sceneModel.meshes[re.value.meshId]!!),
                    transform = Transform(Mat4(re.value.transform))
                )
            }
        }
    }

    private fun FrameContext.materialModifiers(material: SceneModel.Material) =
        listOfNotNull(
            base(
                color = ColorRGBA(material.baseColor),
                colorTexture = material.colorTextureId?.let {
                    texture(texturePrefix + it)
                }
            ),
            material.colorTextureIds?.let {
                colorTextures(textureArray(*it.map { t -> texturePrefix + t }.toTypedArray()))
            }
        )

    private fun FrameContext.mesh(id: String, mesh: SceneModel.Mesh) =
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

