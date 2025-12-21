package com.zakgof.korender.impl.gltf

import com.zakgof.korender.Attributes
import com.zakgof.korender.Attributes.MODEL0
import com.zakgof.korender.Attributes.MODEL1
import com.zakgof.korender.Attributes.MODEL2
import com.zakgof.korender.Attributes.MODEL3
import com.zakgof.korender.IndexType
import com.zakgof.korender.KorenderException
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.impl.absolutizeResource
import com.zakgof.korender.impl.engine.GltfDeclaration
import com.zakgof.korender.impl.engine.Loader
import com.zakgof.korender.impl.geometry.CMesh
import com.zakgof.korender.impl.gl.GLConstants
import com.zakgof.korender.impl.glgpu.toGL
import com.zakgof.korender.math.Mat4
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

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

    fun load(declaration: GltfDeclaration, loader: Loader): GltfCache? =
        loader.syncy(declaration.resource) { load(declaration, declaration.loader) }

    suspend fun load(declaration: GltfDeclaration, appResourceLoader: ResourceLoader): GltfCache {
        val extension = declaration.resource.split(".").last().lowercase() // TODO: autodetect
        val resourceBytes = declaration.loader(declaration.resource)
        val loaded = when (extension) {

            // TODO: autodetect
            "gltf" -> loadGltf(resourceBytes, null, appResourceLoader, declaration)
            "glb" -> loadGlb(resourceBytes, appResourceLoader, declaration)
            else -> throw KorenderException("Unknown extension of gltf/glb resource: $extension")
        }
        return loaded
    }

    private suspend fun loadGlb(
        resourceBytes: ByteArray,
        appResourceLoader: ResourceLoader,
        declaration: GltfDeclaration
    ): GltfCache {
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

        val binData = chunks.find { it.type == ChunkType.BIN }?.data

        return loadGltf(jsonChunk.data, binData, appResourceLoader, declaration)
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

    private suspend fun loadGltf(resourceBytes: ByteArray, binData: ByteArray?, appResourceLoader: ResourceLoader, declaration: GltfDeclaration): GltfCache {
        val gltfCode = resourceBytes.decodeToString()
        val model = json.decodeFromString<InternalGltfModel>(gltfCode)
        val loadedUris = preloadUris(model, appResourceLoader, declaration.resource, binData)
        val loadedAccessors = preloadAccessors(model, loadedUris)
        val loadedSkins = preloadSkins(model, loadedAccessors)
        val loadedMeshes = preloadMeshes(model, loadedAccessors, declaration)
        return GltfCache(model, declaration.resource, loadedUris, loadedAccessors, loadedSkins, loadedMeshes)
    }

    private suspend fun preloadUris(model: InternalGltfModel, appResourceLoader: ResourceLoader, gltfResource: String, binData: ByteArray?) =
        listOfNotNull(
            model.buffers?.mapNotNull { it.uri },
            model.images?.mapNotNull { it.uri }
        )
            .flatten()
            .associateWith { loadUriBytes(appResourceLoader, absolutizeResource(it, gltfResource)) }
            .toMutableMap()
            .apply { binData?.let { this[""] = it } }

    private fun preloadAccessors(model: InternalGltfModel, loadedUris: Map<String, ByteArray>): AccessorCache {
        val all = mutableMapOf<Int, ByteArray>()
        val floats = mutableMapOf<Int, FloatArray>()
        val floatArrays = mutableMapOf<Int, Array<List<Float>>>()

        model.accessors?.forEachIndexed { index, accessor ->
            val raw = getAccessorBytes(model, accessor, loadedUris)
            if (accessor.componentType == GLConstants.GL_FLOAT) {
                val floatArray = raw.asNativeFloatArray()
                when (accessor.type) {
                    "VEC4" -> floatArrays[index] = Array(floatArray.size / 4) { List(4) { i -> floatArray[i + it * 4] } }
                    "VEC3" -> floatArrays[index] = Array(floatArray.size / 3) { List(3) { i -> floatArray[i + it * 3] } }
                    else -> floats[index] = floatArray
                }
            }
            all[index] = raw
        }
        return AccessorCache(all, floats, floatArrays)
    }

    private fun getAccessorBytes(model: InternalGltfModel, accessor: InternalGltfModel.Accessor, loadedUris: Map<String, ByteArray>): ByteArray {

        val componentBytes = accessor.componentByteSize()
        val elementComponents = accessor.elementComponentSize()

        val bufferView = model.bufferViews!![accessor.bufferView!!]
        val buffer = model.buffers!![bufferView.buffer]
        val bufferBytes = loadedUris[buffer.uri ?: ""]!!
        val byteOffset = accessor.byteOffset ?: 0

        val stride = bufferView.byteStride ?: 0
        if (stride == 0 || stride == elementComponents * componentBytes) {
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

    private fun preloadSkins(model: InternalGltfModel, loadedAccessors: AccessorCache) =
        model.skins?.mapIndexed { index, skin ->
            // TODO validate accessor type map4
            index to loadedAccessors.floats[skin.inverseBindMatrices!!]!!.asNativeMat4List()
        }?.toMap() ?: mapOf()


    @OptIn(ExperimentalEncodingApi::class)
    private suspend fun loadUriBytes(appResourceLoader: ResourceLoader, resourceUri: String): ByteArray {
        if (resourceUri.startsWith("data:")) {
            val splitcomma = resourceUri.split(",")
            val data = splitcomma[1]
            val header = splitcomma[0].substring(5)
            val splitsemi = header.split(";")
            val isBase64 = splitsemi.size == 2 && splitsemi[1] == "base64"
            val mediaType = splitsemi[0]
            val bytes = if (isBase64) Base64.decode(data) else data.encodeToByteArray()
            return bytes
        }
        return appResourceLoader(resourceUri)
    }

    private fun preloadMeshes(model: InternalGltfModel, loadedAccessors: AccessorCache, declaration: GltfDeclaration): Map<Pair<Int, Int>, CMesh> =
        model.meshes?.flatMapIndexed { meshIndex, mesh ->
            mesh.primitives.mapIndexed { primitiveIndex, primitive ->
                val indicesAccessor = primitive.indices?.let { model.accessors!![it] }
                val verticesAttributeAccessors = primitive.attributes.mapNotNull { p ->
                    val accessor = model.accessors!![p.value]
                    attributeForAccessor(p.key, accessor)?.let { it to p.value }
                }
                val attributes = verticesAttributeAccessors.map { it.first }.toMutableList()
                if (declaration.instancingDeclaration != null) {
                    attributes += listOf(MODEL0, MODEL1, MODEL2, MODEL3)
                }

                val cMesh = CMesh(
                    model.accessors!![verticesAttributeAccessors.first().second].count,
                    indicesAccessor?.count ?: 0,
                    declaration.instancingDeclaration?.count ?: -1,
                    *attributes.toTypedArray(),
                    indexType = accessorComponentTypeToIndexType(indicesAccessor?.componentType)
                ) {
                    indicesAccessor?.let { indexBytes(loadedAccessors.all[primitive.indices]!!) }
                    verticesAttributeAccessors.forEach {
                        attrBytes(it.first, loadedAccessors.all[it.second]!!)
                    }
                }
                (meshIndex to primitiveIndex) to cMesh
            }
        }?.toMap() ?: mapOf()


    private fun attributeForAccessor(key: String, accessor: InternalGltfModel.Accessor): MeshAttribute<*>? {
        val candidates = when (key) {
            "POSITION" -> listOf(Attributes.POS)
            "NORMAL" -> listOf(Attributes.NORMAL)
            "TEXCOORD_0" -> listOf(Attributes.TEX)
            "JOINTS_0" -> listOf(Attributes.JOINTS_BYTE, Attributes.JOINTS_SHORT, Attributes.JOINTS_INT)
            "WEIGHTS_0" -> listOf(Attributes.WEIGHTS)
            else -> null
        }
        return candidates?.firstOrNull {
            it.structSize == accessor.elementComponentSize()
                    && it.primitiveType.toGL() == accessor.componentType
        }
    }

    private fun accessorComponentTypeToIndexType(componentType: Int?) =
        when (componentType) {
            null -> null
            GLConstants.GL_UNSIGNED_BYTE -> IndexType.Byte
            GLConstants.GL_UNSIGNED_SHORT -> IndexType.Short
            GLConstants.GL_UNSIGNED_INT -> IndexType.Int
            else -> throw KorenderException("GLTF: Unsupported componentType for index: $componentType")
        }
}

internal fun InternalGltfModel.Accessor.componentByteSize(): Int =
    when (componentType) {
        GLConstants.GL_UNSIGNED_BYTE -> 1
        GLConstants.GL_UNSIGNED_SHORT -> 2
        GLConstants.GL_UNSIGNED_INT -> 4
        GLConstants.GL_FLOAT -> 4
        else -> throw KorenderException("GLTF: Not supported accessor componentType $componentType")
    }

internal fun InternalGltfModel.Accessor.elementComponentSize(): Int =
    // TODO enums
    when (type) {
        "SCALAR" -> 1
        "VEC2" -> 2
        "VEC3" -> 3
        "VEC4" -> 4
        "MAT3" -> 9
        "MAT4" -> 16
        else -> throw KorenderException("GLTF: Not supported accessor type $type")
    }


// TODO move me and optimize by avoiding copy
internal fun FloatArray.asNativeMat4List(): List<Mat4> =
    List(size / 16) { m ->
        Mat4(this.copyOfRange(m * 16, m * 16 + 16))
    }

// TODO move me
internal fun ByteArray.asNativeFloatArray() =
    FloatArray(size / 4) {
        Float.fromBits(
            (this[it * 4 + 0].toInt() and 0xFF) or
                    ((this[it * 4 + 1].toInt() and 0xFF) shl 8) or
                    ((this[it * 4 + 2].toInt() and 0xFF) shl 16) or
                    ((this[it * 4 + 3].toInt() and 0xFF) shl 24)
        )
    }
