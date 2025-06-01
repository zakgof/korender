package com.zakgof.korender.impl.gltf

import com.zakgof.korender.KorenderException
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.impl.absolutizeResource
import com.zakgof.korender.impl.engine.GltfDeclaration
import com.zakgof.korender.impl.gl.GLConstants
import com.zakgof.korender.impl.resourceBytes
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
        gltfResource: String
    ): GltfLoaded {
        val gltfCode = resourceBytes.decodeToString()
        val model = json.decodeFromString<Gltf>(gltfCode)
        val loadedUris = listOfNotNull(
            model.buffers?.mapNotNull { it.uri },
            model.images?.mapNotNull { it.uri }
        )
            .flatten()
            .associateWith { loadUriBytes(appResourceLoader, absolutizeResource(it, gltfResource)) }
        return GltfLoaded(model, gltfResource, loadedUris.toMutableMap())
    }

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
        return resourceBytes(appResourceLoader, resourceUri)
    }
}

fun Gltf.Accessor.componentByteSize(): Int =
    when (componentType) {
        GLConstants.GL_UNSIGNED_BYTE -> 1
        GLConstants.GL_UNSIGNED_SHORT -> 2
        GLConstants.GL_UNSIGNED_INT -> 4
        GLConstants.GL_FLOAT -> 4
        else -> throw KorenderException("GLTF: Not supported accessor componentType $componentType")
    }

fun Gltf.Accessor.elementComponentSize(): Int =
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
