package com.zakgof.korender.impl.gltf

import com.zakgof.korender.KorenderException
import com.zakgof.korender.gl.GLConstants
import com.zakgof.korender.impl.engine.Bucket
import com.zakgof.korender.impl.engine.GltfDeclaration
import com.zakgof.korender.impl.engine.Inventory
import com.zakgof.korender.impl.engine.RenderableDeclaration
import com.zakgof.korender.impl.parentResourceOf
import com.zakgof.korender.impl.resourceBytes
import com.zakgof.korender.material.MaterialBuilder
import com.zakgof.korender.material.MaterialModifiers
import com.zakgof.korender.material.StandartMaterialOption
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Transform
import com.zakgof.korender.mesh.Attributes
import com.zakgof.korender.mesh.CustomMesh
import com.zakgof.korender.mesh.MeshDeclaration
import kotlinx.serialization.json.Json

internal object GltfLoader {

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
        val loadedUris = listOfNotNull(
            model.buffers?.mapNotNull { it.uri },
            model.images?.mapNotNull { it.uri }
        )
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

    val renderableDeclarations = mutableListOf<RenderableDeclaration>()

    fun build(): MutableList<RenderableDeclaration> {
        val model = gltfLoaded.model
        val scene = model.scenes!![model.scene]
        scene.nodes.map { model.nodes!![it] }
            .forEach { processNode(Transform(), it) }

        return renderableDeclarations
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

            val meshDeclaration = createMeshDeclaration(primitive, meshIndex, primitiveIndex)
            val material = primitive.material?.let { gltfLoaded.model.materials!![it] }

            val materialDeclaration = MaterialBuilder().apply {
                MaterialModifiers.standart(
                    StandartMaterialOption.FixedColor
                ) {
                    color = Color.Red
                }.applyTo(this)
            }.toMaterialDeclaration()

            val renderableDeclaration = RenderableDeclaration(
                meshDeclaration,
                materialDeclaration.shader,
                materialDeclaration.uniforms,
                transform,
                Bucket.OPAQUE
            )

            renderableDeclarations += renderableDeclaration
        }
    }

    private fun createMeshDeclaration(
        primitive: Gltf.Mesh.Primitive,
        meshIndex: Int,
        primitiveIndex: Int
    ): MeshDeclaration {
        val indicesAccessor = primitive.indices?.let { gltfLoaded.model.accessors!![it] }
        val verticesAttributeAccessors = primitive.attributes
            .map { Attributes.byGltfName(it.key) to gltfLoaded.model.accessors!![it.value] }
            .toMap()

        val attrs = verticesAttributeAccessors.keys.toList()

        return CustomMesh(
            "$resource:$meshIndex:$primitiveIndex",
            verticesAttributeAccessors.entries.first().value.count,
            indicesAccessor!!.count,
            attrs,
            false
        ) {
            indexBytes(getAccessorBytes(indicesAccessor))
            attrs.forEach {
                attrBytes(it, getAccessorBytes(verticesAttributeAccessors[it]!!))
            }
        }
    }

    private fun getAccessorBytes(accessor: Gltf.Accessor): ByteArray {

        val componentBytes = accessor.componentByteSize()
        val elementComponents = accessor.elementComponentSize()

        val bufferView = gltfLoaded.model.bufferViews!![accessor.bufferView!!]
        val buffer = gltfLoaded.model.buffers!![bufferView.buffer]
        val bufferBytes = gltfLoaded.loadedUris[buffer.uri]!!

        val stride = bufferView.byteStride ?: 0
        if (stride == 0) {
            return bufferBytes.copyOfRange(
                bufferView.byteOffset + accessor.byteOffset,
                bufferView.byteOffset + accessor.byteOffset + accessor.count * elementComponents * componentBytes
            )
        } else {
            val accessorBytes = ByteArray(accessor.count * elementComponents * componentBytes)
            for (element in 0 until accessor.count) {
                bufferBytes.copyInto(
                    accessorBytes,
                    element * elementComponents * componentBytes,
                    bufferView.byteOffset + element * stride + accessor.byteOffset,
                    bufferView.byteOffset + element * stride + accessor.byteOffset +
                            elementComponents * componentBytes
                )
            }
            return accessorBytes
        }
    }
}

fun Gltf.Accessor.componentByteSize(): Int =
    when (componentType) {
        // TODO https://github.com/KhronosGroup/glTF/blob/main/specification/2.0/schema/accessor.schema.json
        // TODO force SHORT/INT indexes into IB
        GLConstants.GL_UNSIGNED_BYTE -> 1
        GLConstants.GL_UNSIGNED_SHORT -> 2
        GLConstants.GL_FLOAT -> 4
        else -> throw KorenderException("GLTF: Not supported accessor componentType $componentType")
    }

fun Gltf.Accessor.elementComponentSize(): Int =
    when (type) {
        // TODO https://github.com/KhronosGroup/glTF/blob/main/specification/2.0/schema/accessor.schema.json
        "SCALAR" -> 1
        "VEC2" -> 2
        "VEC3" -> 3
        else -> throw KorenderException("GLTF: Not supported accessor type $type")
    }
