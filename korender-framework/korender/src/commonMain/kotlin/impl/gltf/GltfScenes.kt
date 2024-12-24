package com.zakgof.korender.impl.gltf

import com.zakgof.korender.impl.engine.GltfDeclaration
import com.zakgof.korender.impl.engine.Inventory
import com.zakgof.korender.impl.parentResourceOf
import com.zakgof.korender.impl.resourceBytes
import com.zakgof.korender.math.Transform
import com.zakgof.korender.mesh.Attributes
import com.zakgof.korender.mesh.CustomMesh
import kotlinx.serialization.json.Json

internal object GltfScenes {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun load(
        declaration: GltfDeclaration,
        appResourceLoader: suspend (String) -> ByteArray
    ): GltfLoaded {
        val gltfCode = resourceBytes(appResourceLoader, declaration.gltfResource)
            .decodeToString()
        val model = json.decodeFromString<Gltf>(gltfCode)
        val loadedUris = listOf(
            model.buffers?.mapNotNull { it.uri },
            model.images?.mapNotNull { it.uri }
        ).filterNotNull()
            .flatten()
            .associateWith {
                resourceBytes(
                    appResourceLoader,
                    it,
                    parentResourceOf(declaration.gltfResource)
                )
            }
        return GltfLoaded(model, loadedUris)
    }
}

internal class GltfSceneBuilder(
    val inventory: Inventory,
    val resource: String,
    val gltfLoaded: GltfLoaded
) {
    fun build() {
        val model = gltfLoaded.model
        val scene = model.scenes!![model.scene]
        scene.nodes.map { model.nodes!![it] }
            .forEach { processNode(Transform(), it) }

    }

    private fun processNode(transform: Transform, node: Gltf.Node) {

        val mesh = node.mesh
            ?.let { gltfLoaded.model.meshes!![it] }
            ?.let { processMesh(transform, it, node.mesh) }

        node.children?.map { gltfLoaded.model.nodes!![it] }
            ?.forEach { processNode(transform, it) }
    }

    private fun processMesh(transform: Transform, mesh: Gltf.Mesh, meshIndex: Int) {
        mesh.primitives.forEachIndexed { primitiveIndex, primitive ->

            val indicesAccessor = primitive.indices?.let { gltfLoaded.model.accessors!![it] }
            val verticesAttributeAccessors = primitive.attributes
                .mapValues { gltfLoaded.model.accessors!![it.value] }

            val meshDeclaration = CustomMesh(
                "$resource:$meshIndex:$primitiveIndex",
                verticesAttributeAccessors.entries.first().value.count,
                indicesAccessor!!.count,
                primitive.attributes.keys.map { Attributes.byName(it)!! },
                false
            ) {
                // indices(getAccessorBytes(indicesAccessor))
                // vertices(getAccessorBytes())
            }
            val loadedMesh = inventory.mesh(meshDeclaration)

            val material = primitive.material?.let { gltfLoaded.model.materials!![it] }
            // TODO primitive.mode
            // TODO primitive.targets

        }
    }

}