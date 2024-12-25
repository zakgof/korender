package com.zakgof.korender.impl.gltf

import com.zakgof.korender.KorenderException
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.gl.GLConstants
import com.zakgof.korender.impl.engine.Bucket
import com.zakgof.korender.impl.engine.GltfDeclaration
import com.zakgof.korender.impl.engine.Inventory
import com.zakgof.korender.impl.engine.MaterialDeclaration
import com.zakgof.korender.impl.engine.RenderableDeclaration
import com.zakgof.korender.impl.material.ByteArrayTextureDeclaration
import com.zakgof.korender.impl.parentResourceOf
import com.zakgof.korender.impl.resourceBytes
import com.zakgof.korender.material.MaterialBuilder
import com.zakgof.korender.material.MaterialModifiers
import com.zakgof.korender.material.StandartMaterialOption
import com.zakgof.korender.material.TextureDeclaration
import com.zakgof.korender.material.TextureFilter
import com.zakgof.korender.material.TextureWrap
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

    enum class ChunkType(val value: Int) {
        JSON(0x4E4F534A),
        BIN(0x004E4942),
        UNKNOWN(-1)
    }

    class GlbChunk(val type: ChunkType, val data: ByteArray)

    suspend fun load(
        declaration: GltfDeclaration,
        appResourceLoader: ResourceLoader
    ): GltfLoaded {
        val extension = declaration.gltfResource.split(".").last().lowercase()
        val resourceBytes = resourceBytes(appResourceLoader, declaration.gltfResource)
        return when (extension) {
            "gltf" -> loadGltf(resourceBytes, appResourceLoader, declaration.gltfResource)
            "glb" -> loadGlb(resourceBytes, appResourceLoader, declaration.gltfResource)
            else -> throw KorenderException("Unknown extension of gltf/glb resource: $extension")
        }
    }

    private suspend fun loadGlb(
        resourceBytes: ByteArray,
        appResourceLoader: ResourceLoader,
        resourceName: String
    ): GltfLoaded {
        val reader = ByteArrayReader(resourceBytes)

        readGlbHeader(reader, resourceBytes)

        val chunks = mutableListOf<GlbChunk>()
        while (reader.hasRemaining()) {
            val chunkLength = reader.readUInt32()
            val chunkType = reader.readUInt32()
            val chunkData = reader.readBytes(chunkLength)
            val type = when (chunkType) {
                ChunkType.JSON.value -> ChunkType.JSON
                ChunkType.BIN.value -> ChunkType.BIN
                else -> ChunkType.UNKNOWN
            }
            chunks.add(GlbChunk(type, chunkData))
        }

        val jsonChunk = chunks.find { it.type == ChunkType.JSON }
            ?: throw KorenderException("Missing JSON chunk in GLB file")

        return loadGltf(jsonChunk.data, appResourceLoader, resourceName).apply {
            chunks.find { it.type == ChunkType.BIN }?.let {
                loadedUris[""] = it.data
            }
        }
    }

    private fun readGlbHeader(reader: ByteArrayReader, resourceBytes: ByteArray) {
        val magic = reader.readUInt32()
        if (magic != 0x46546C67) { // ASCII "glTF"
            throw KorenderException("Invalid GLB file magic: $magic")
        }

        val version = reader.readUInt32()
        if (version != 2) {
            throw KorenderException("Unsupported GLB version: $version")
        }

        val length = reader.readUInt32()
        if (length != resourceBytes.size) {
            throw KorenderException("GLB file length mismatch")
        }
    }

    private suspend fun loadGltf(
        resourceBytes: ByteArray,
        appResourceLoader: ResourceLoader,
        resourceName: String
    ): GltfLoaded {
        val gltfCode = resourceBytes.decodeToString()
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
                    parentResourceOf(resourceName)
                )
            }
        return GltfLoaded(model, loadedUris.toMutableMap())
    }
}

internal class GltfSceneBuilder(
    val inventory: Inventory,
    private val resource: String,
    private val gltfLoaded: GltfLoaded
) {

    private val renderableDeclarations = mutableListOf<RenderableDeclaration>()

    fun build(): MutableList<RenderableDeclaration> {
        val model = gltfLoaded.model
        val scene = model.scenes!![model.scene]
        scene.nodes.map { model.nodes!![it] }
            .forEach { processNode(Transform().scale(3.0f), it) } // TODO debug

        model.materials?.map {
            it.pbrMetallicRoughness?.baseColorTexture
        }

        return renderableDeclarations
    }

    private fun processNode(transform: Transform, node: Gltf.Node) {

        node.mesh
            ?.let { gltfLoaded.model.meshes!![it] }
            ?.let { processMesh(transform, it, node.mesh) }

        node.children?.map { gltfLoaded.model.nodes!![it] }
            ?.forEach { processNode(transform, it) }
    }

    private fun processMesh(transform: Transform, mesh: Gltf.Mesh, meshIndex: Int) {
        mesh.primitives.forEachIndexed { primitiveIndex, primitive ->

            val meshDeclaration = createMeshDeclaration(primitive, meshIndex, primitiveIndex)
            val materialDeclaration = createMaterialDeclaration(primitive)


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

    private fun createMaterialDeclaration(primitive: Gltf.Mesh.Primitive): MaterialDeclaration {

        val material = primitive.material?.let { gltfLoaded.model.materials!![it] }

        val pbr = material?.pbrMetallicRoughness

        val metallic = pbr?.metallicFactor ?: 0.3f
        val roughness = pbr?.roughnessFactor ?: 0.3f
        val emissiveFactor =
            material?.emissiveFactor?.let { Color(1.0f, it[0], it[1], it[2]) } ?: Color.Black
        val baseColor =
            pbr?.baseColorFactor?.let { Color(it[0], it[1], it[2], it[3]) } ?: Color.White
        val albedoTexture = pbr?.baseColorTexture?.let { getTexture(it) }
        val metallicRoughnessTexture = pbr?.metallicRoughnessTexture?.let { getTexture(it) }
        val normalTexture = material?.normalTexture?.let { getTexture(it) }
        val occlusionTexture = material?.occlusionTexture?.let { getTexture(it) }
        val emissiveTexture = material?.emissiveTexture?.let { getTexture(it) }

        val flags = mapOf(
            albedoTexture to StandartMaterialOption.AlbedoMap,
            metallicRoughnessTexture to StandartMaterialOption.MetallicRoughnessMap,
            normalTexture to StandartMaterialOption.NormalMap,
            occlusionTexture to StandartMaterialOption.OcclusionMap,
            emissiveTexture to StandartMaterialOption.EmissiveMap
        ).filterKeys { it != null }
            .values
            .toTypedArray()

        return MaterialBuilder().apply {
            MaterialModifiers.standart(*flags) {
                this.metallic = metallic
                this.roughness = roughness
                this.baseColor = baseColor
                this.albedoTexture = albedoTexture
                this.emissiveFactor = emissiveFactor
                this.metallicRoughnessTexture = metallicRoughnessTexture
                this.normalTexture = normalTexture
                this.occlusionTexture = occlusionTexture
                this.emissiveTexture = emissiveTexture

            }.applyTo(this)
        }.toMaterialDeclaration()

    }

    private fun createMeshDeclaration(
        primitive: Gltf.Mesh.Primitive,
        meshIndex: Int,
        primitiveIndex: Int
    ): MeshDeclaration {
        val indicesAccessor = primitive.indices?.let { gltfLoaded.model.accessors!![it] }
        val verticesAttributeAccessors = primitive.attributes
            .mapNotNull { p ->
                Attributes.byGltfName(p.key)?.let { it to gltfLoaded.model.accessors!![p.value] }
            }

        return CustomMesh(
            "$resource:$meshIndex:$primitiveIndex",
            verticesAttributeAccessors.first().second.count,
            indicesAccessor!!.count,
            verticesAttributeAccessors.map { it.first }.sortedBy { it.order },
            false,
            accessorComponentTypeToLongIndex(indicesAccessor.componentType)
        ) {
            indexBytes(getAccessorBytes(indicesAccessor))
            verticesAttributeAccessors.forEach {
                attrBytes(it.first, getAccessorBytes(it.second))
            }
        }
    }

    private fun accessorComponentTypeToLongIndex(componentType: Int) =
        when (componentType) {
            GLConstants.GL_UNSIGNED_SHORT -> false
            GLConstants.GL_UNSIGNED_INT -> true
            else -> throw KorenderException("GLTF: Unsupported componentType for index: $componentType")
        }

    private fun getAccessorBytes(accessor: Gltf.Accessor): ByteArray {

        val componentBytes = accessor.componentByteSize()
        val elementComponents = accessor.elementComponentSize()

        val bufferView = gltfLoaded.model.bufferViews!![accessor.bufferView!!]
        val buffer = gltfLoaded.model.buffers!![bufferView.buffer]
        val bufferBytes = getBufferBytes(buffer)
        val byteOffset = accessor.byteOffset ?: 0

        val stride = bufferView.byteStride ?: 0
        if (stride == 0) {
            return bufferBytes.copyOfRange(
                bufferView.byteOffset + byteOffset,
                bufferView.byteOffset + byteOffset + accessor.count * elementComponents * componentBytes
            )
        } else {
            val accessorBytes = ByteArray(accessor.count * elementComponents * componentBytes)
            for (element in 0 until accessor.count) {
                bufferBytes.copyInto(
                    accessorBytes,
                    element * elementComponents * componentBytes,
                    bufferView.byteOffset + element * stride + byteOffset,
                    bufferView.byteOffset + element * stride + byteOffset +
                            elementComponents * componentBytes
                )
            }
            return accessorBytes
        }
    }

    private fun getBufferBytes(buffer: Gltf.Buffer): ByteArray {
        if (buffer.uri == null)
            return gltfLoaded.loadedUris[""]!!
        // TODO: support base64 inline data
        return gltfLoaded.loadedUris[buffer.uri]!!
    }

    private fun getBufferViewBytes(bufferView: Gltf.BufferView): ByteArray {
        val buffer = gltfLoaded.model.buffers!![bufferView.buffer]
        val bufferBytes = getBufferBytes(buffer)
        return bufferBytes.copyOfRange(bufferView.byteOffset, bufferView.byteOffset + bufferView.byteLength)
    }

    private fun getTexture(ti: Gltf.TextureIndexProvider): TextureDeclaration? {
        val image = gltfLoaded.model.textures?.get(ti.index)?.source
            ?.let { src -> gltfLoaded.model.images!![src] }

       return image?.let { img ->
            val bytes = getImageBytes(img)
            ByteArrayTextureDeclaration(
                img.uri ?: "TODO ${ti.index}", // TODO !!!
                TextureFilter.MipMapLinearLinear,
                TextureWrap.Repeat,
                1024,
                bytes,
                img.mimeType!!.split("/").last()
            )
        }
    }

    private fun getImageBytes(image: Gltf.Image): ByteArray {
        if (image.uri != null)
            return gltfLoaded.loadedUris[image.uri]!!
        if (image.bufferView != null) {
            val bufferView = gltfLoaded.model.bufferViews!![image.bufferView]
            return getBufferViewBytes(bufferView)
        }
        throw KorenderException("GLTF: image without uri or bufferView")
    }
}

fun Gltf.Accessor.componentByteSize(): Int =
    when (componentType) {
        // TODO https://github.com/KhronosGroup/glTF/blob/main/specification/2.0/schema/accessor.schema.json
        // TODO force SHORT/INT indexes into IB
        GLConstants.GL_UNSIGNED_BYTE -> 1
        GLConstants.GL_UNSIGNED_SHORT -> 2
        GLConstants.GL_UNSIGNED_INT -> 4
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
