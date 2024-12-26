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
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.mesh.Attributes
import com.zakgof.korender.mesh.CustomMesh
import com.zakgof.korender.mesh.MeshDeclaration
import com.zakgof.korender.mesh.Meshes
import kotlinx.serialization.json.Json
import kotlin.math.floor

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
            .associateWith { resourceBytes(appResourceLoader, it, parentResourceOf(resourceName)) }
        return GltfLoaded(model, loadedUris.toMutableMap())
    }
}

internal class GltfSceneBuilder(
    val inventory: Inventory,
    private val resource: String,
    private val gltfLoaded: GltfLoaded
) {

    private val renderableDeclarations = mutableListOf<RenderableDeclaration>()
    private val nodeAnimations =
        mutableMapOf<Int, MutableMap<String, List<Float>>>()

    fun build(time: Float): MutableList<RenderableDeclaration> {
        val model = gltfLoaded.model
        val scene = model.scenes!![model.scene]
        model.skins?.forEach { skin ->
            skin.joints.associateWith { }
        }
        model.animations?.forEach { animation ->
            val samplerValues = animation.samplers.map { sampler -> getSamplerValue(sampler, time) }
            animation.channels.forEach { channel ->
                nodeAnimations.getOrPut(channel.target.node!!) {
                    mutableMapOf()
                }[channel.target.path] = samplerValues[channel.sampler]
            }
        }
        scene.nodes.forEach { nodeIndex ->
            processNode(Transform().scale(0.5f), nodeIndex, model.nodes!![nodeIndex]) // TODO debug
        }
        return renderableDeclarations
    }

    private fun ByteArray.asNativeFloatList(): List<Float> = List<Float>(size / 4) {
        Float.fromBits(
            (this[it * 4 + 0].toInt() and 0xFF) or
                    ((this[it * 4 + 1].toInt() and 0xFF) shl 8) or
                    ((this[it * 4 + 2].toInt() and 0xFF) shl 16) or
                    ((this[it * 4 + 3].toInt() and 0xFF) shl 24)
        )
    }

    // TODO: PERF !! - preload add the samplers, no need to to each frame
    private fun getSamplerValue(
        sampler: Gltf.Animation.AnimationSampler,
        currentTime: Float
    ): List<Float> {

        val inputAccessor = gltfLoaded.model.accessors!![sampler.input]
        val inputBytes = getAccessorBytes(inputAccessor)
        val inputFloats = inputBytes.asNativeFloatList()

        val outputAccessor = gltfLoaded.model.accessors[sampler.output]
        val outputBytes = getAccessorBytes(outputAccessor)
        val outputFloats = outputBytes.asNativeFloatList()
        // TODO validate float input and output
        val outputValues = getAccessorFloatBasedElements(outputFloats, outputAccessor.type)
        // TODO validate same lengths

        val max = inputFloats.last() * 2.0f
        val timeOffset = currentTime - floor(currentTime / max) * max

        var samplerPositionBefore =
            inputFloats.indexOfLast { timeOffset > it } // TODO linear scan - ineffective!
        if (samplerPositionBefore < 0) samplerPositionBefore = 0

        // TODO this is STEP, implement other interpolations
        val output = outputValues[samplerPositionBefore]
        return output

    }

    private fun getAccessorFloatBasedElements(raw: List<Float>, type: String): List<List<Float>> =
        when (type) {
            "VEC4" -> List(raw.size / 4) { List(4) { i -> raw[i + it * 4] } }
            "VEC3" -> List(raw.size / 3) { List(3) { i -> raw[i + it * 3] } }
            else -> throw KorenderException("GLTF: Unknown accessor element type for sampler: $type")
        }

    private fun processNode(parentTransform: Transform, nodeIndex: Int, node: Gltf.Node) {
        var transform = parentTransform

        val na = nodeAnimations[nodeIndex]

        val translation = na?.get("translation") ?: node.translation
        val rotation = na?.get("rotation") ?: node.rotation
        val scale = na?.get("scale") ?: node.scale


        if (na != null)
            println("node $nodeIndex rotation $rotation")

        translation?.let { transform *= Transform.translate(Vec3(it[0], it[1], it[2])) }
        rotation?.let {
            transform *= Transform.rotate(
                Quaternion(
                    it[3],
                    Vec3(it[0], it[1], it[2])
                )
            )
        }
        scale?.let { transform *= Transform.scale(it[0], it[1], it[2]) }

        node.matrix?.let { transform *= Transform(Mat4(it.toFloatArray())) }
        node.mesh
            ?.let { gltfLoaded.model.meshes!![it] }
            ?.let { processMesh(transform, it, node.mesh) }
        node.children?.forEach { childNodeIndex ->
            processNode(transform, childNodeIndex, gltfLoaded.model.nodes!![childNodeIndex])
        }
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
                Bucket.OPAQUE // TODO transparent mode
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
            pbr?.baseColorFactor?.let { Color(it[3], it[0], it[1], it[2]) } ?: Color.White
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
            accessorComponentTypeToIndexType(indicesAccessor.componentType)
        ) {
            indexBytes(getAccessorBytes(indicesAccessor))
            verticesAttributeAccessors.forEach {
                attrBytes(it.first, getAccessorBytes(it.second))
            }
        }
    }

    private fun accessorComponentTypeToIndexType(componentType: Int) =
        when (componentType) {
            GLConstants.GL_UNSIGNED_BYTE -> Meshes.IndexType.Byte
            GLConstants.GL_UNSIGNED_SHORT -> Meshes.IndexType.Short
            GLConstants.GL_UNSIGNED_INT -> Meshes.IndexType.Int
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
        return bufferBytes.copyOfRange(
            bufferView.byteOffset,
            bufferView.byteOffset + bufferView.byteLength
        )
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
        else -> throw KorenderException("GLTF: Not supported accessor type $type")
    }
