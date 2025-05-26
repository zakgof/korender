package com.zakgof.korender.impl.gltf

import com.zakgof.korender.Attributes.JOINTS_BYTE
import com.zakgof.korender.Attributes.JOINTS_INT
import com.zakgof.korender.Attributes.JOINTS_SHORT
import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.Attributes.WEIGHTS
import com.zakgof.korender.IndexType
import com.zakgof.korender.KorenderException
import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.impl.absolutizeResource
import com.zakgof.korender.impl.engine.BaseMaterial
import com.zakgof.korender.impl.engine.Bucket
import com.zakgof.korender.impl.engine.GltfDeclaration
import com.zakgof.korender.impl.engine.RenderableDeclaration
import com.zakgof.korender.impl.geometry.CustomMesh
import com.zakgof.korender.impl.gl.GLConstants
import com.zakgof.korender.impl.glgpu.Mat4List
import com.zakgof.korender.impl.glgpu.toGL
import com.zakgof.korender.impl.material.ByteArrayTextureDeclaration
import com.zakgof.korender.impl.material.InternalMaterialModifier
import com.zakgof.korender.impl.resourceBytes
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.floor

class LoadedSkin(val inverseBindMatrices: List<Mat4>, var jointMatrices: List<Mat4>)

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
    private suspend fun loadUriBytes(
        appResourceLoader: ResourceLoader,
        resourceUri: String
    ): ByteArray {
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

internal class GltfSceneBuilder(
    private val declaration: GltfDeclaration,
    private val gltfLoaded: GltfLoaded,
) {
    private val nodeMatrices = mutableMapOf<Int, Transform>()
    private val meshes = mutableListOf<Pair<Int, Int>>()
    private val renderableDeclarations = mutableListOf<RenderableDeclaration>()
    private val nodeAnimations =
        mutableMapOf<Int, MutableMap<String, List<Float>>>()
    private val loadedSkins = mutableListOf<LoadedSkin>()

    fun build(time: Float): MutableList<RenderableDeclaration> {
        val model = gltfLoaded.model
        val scene = model.scenes!![model.scene]

        // TODO preload it!
        model.skins?.map { skin ->
            // TODO validate accessor type map4
            val inverseBindMatrices =
                getAccessorBytes(gltfLoaded.model.accessors!![skin.inverseBindMatrices!!]).asNativeMat4List()
            LoadedSkin(inverseBindMatrices, listOf()) // TODO separate these two things
        }?.let { loadedSkins.addAll(it) }

        if (declaration.animation < (model.animations?.size ?: 0)) {
            val animation = model.animations!![declaration.animation]
            val samplerValues = animation.samplers.map { sampler -> getSamplerValue(sampler, time) }
            animation.channels.forEach { channel ->
                nodeAnimations.getOrPut(channel.target.node!!) {
                    mutableMapOf()
                }[channel.target.path] = samplerValues[channel.sampler]
            }
        }

        scene.nodes.forEach { nodeIndex ->
            processNode(
                declaration.transform,
                nodeIndex,
                model.nodes!![nodeIndex]
            )
        }
        model.skins?.mapIndexed { skinIndex, skin ->
            loadedSkins[skinIndex].jointMatrices = skin.joints.map { nodeMatrices[it]!!.mat4 }
        }
        meshes.forEach {
            val meshIndex = it.first
            val nodeIndex = it.second
            val nodeTransform = nodeMatrices[nodeIndex]!!
            processMesh(
                nodeTransform,
                gltfLoaded.model.meshes!![meshIndex],
                meshIndex,
                gltfLoaded.model.nodes!![nodeIndex].skin
            )
        } // TODO map instead

        return renderableDeclarations
    }

    // TODO move me
    private fun ByteArray.asNativeFloatList(): List<Float> = List(size / 4) {
        Float.fromBits(
            (this[it * 4 + 0].toInt() and 0xFF) or
                    ((this[it * 4 + 1].toInt() and 0xFF) shl 8) or
                    ((this[it * 4 + 2].toInt() and 0xFF) shl 16) or
                    ((this[it * 4 + 3].toInt() and 0xFF) shl 24)
        )
    }

    // TODO move me and optimize by avoiding copy
    private fun ByteArray.asNativeMat4List(): List<Mat4> = List(size / 64) { m ->
        Mat4(this.copyOfRange(m * 64, m * 64 + 64).asNativeFloatList().toFloatArray())
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

        val max = inputFloats.last()
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

        translation?.let { transform *= Transform.translate(Vec3(it[0], it[1], it[2])) }
        rotation?.let {
            transform *= Transform.rotate(
                Quaternion(it[3], Vec3(it[0], it[1], it[2]))
            )
        }
        scale?.let { transform *= Transform.scale(it[0], it[1], it[2]) }

        node.matrix?.let { transform *= Transform(Mat4(it.toFloatArray())) }
        nodeMatrices[nodeIndex] = transform

        node.mesh?.let { meshes.add(it to nodeIndex) }

        node.children?.forEach { childNodeIndex ->
            processNode(transform, childNodeIndex, gltfLoaded.model.nodes!![childNodeIndex])
        }
    }

    private fun processMesh(
        transform: Transform,
        mesh: Gltf.Mesh,
        meshIndex: Int,
        skinIndex: Int?
    ) {
        mesh.primitives.forEachIndexed { primitiveIndex, primitive ->
            val meshDeclaration = createMeshDeclaration(primitive, meshIndex, primitiveIndex)
            val materialModifier = createMaterialModifiers(
                primitive,
                skinIndex
            )
            val renderableDeclaration = RenderableDeclaration(
                BaseMaterial.Renderable,
                listOf(materialModifier),
                mesh = meshDeclaration,
                transform = transform,
                bucket = Bucket.OPAQUE, // TODO transparent mode
                declaration.retentionPolicy
            )
            renderableDeclarations += renderableDeclaration
        }
    }

    private fun createMaterialModifiers(
        primitive: Gltf.Mesh.Primitive,
        skinIndex: Int?
    ): MaterialModifier {

        // TODO: split into 2 parts, precompute textures modifier, calc only skin modifier
        val material = primitive.material?.let { gltfLoaded.model.materials!![it] }
        val matPbr = material?.pbrMetallicRoughness
        val matSpecularGlossiness = material?.extensions?.get("KHR_materials_pbrSpecularGlossiness")
                as? Gltf.KHRMaterialsPbrSpecularGlossiness


        // TODO: Precreate all except jointMatrices
        return InternalMaterialModifier { mb ->

            if (skinIndex != null) {
                mb.shaderDefs += "SKINNING"
                mb.uniforms["jntMatrices[0]"] = Mat4List(
                    loadedSkins[skinIndex].jointMatrices.mapIndexed { ind, jm ->
                        jm * loadedSkins[skinIndex].inverseBindMatrices[ind]
                    }
                )
            }

            mb.uniforms["baseColor"] = (matSpecularGlossiness?.diffuseFactor ?: matPbr?.baseColorFactor)?.let {
                ColorRGBA(it[0], it[1], it[2], it[3])
            } ?: ColorRGBA.White

            (matPbr?.baseColorTexture ?: matSpecularGlossiness?.diffuseTexture)?.let { getTexture(it) }?.let {
                mb.uniforms["baseColorTexture"] = it
                mb.shaderDefs += "BASE_COLOR_MAP";
            }

            mb.uniforms["metallicFactor"] = matPbr?.metallicFactor ?: 0.1f
            mb.uniforms["roughnessFactor"] = matPbr?.roughnessFactor ?: 0.5f

            material?.normalTexture?.let { getTexture(it) }?.let {
                mb.plugins["normal"] = "!shader/plugin/normal.texture.frag"
                mb.uniforms["normalTexture"] = it
            }
            matPbr?.metallicRoughnessTexture?.let { getTexture(it) }?.let {
                mb.plugins["metallic_roughness"] = "!shader/plugin/metallic_roughness.texture.frag"
                mb.uniforms["metallicRoughnessTexture"] = it
            }

            // TODO
            val occlusionTexture = material?.occlusionTexture?.let { getTexture(it) }
            val emissiveTexture = material?.emissiveTexture?.let { getTexture(it) }

            matSpecularGlossiness?.let { sg ->
                mb.plugins["specular_glossiness"] = "!shader/plugin/specular_glossiness.factor.frag"
                mb.uniforms["specularFactor"] = sg.specularFactor.let { ColorRGB(it[0], it[1], it[2]) }
                mb.uniforms["glossinessFactor"] = sg.glossinessFactor
            }
        }
    }

    private fun createMeshDeclaration(
        primitive: Gltf.Mesh.Primitive,
        meshIndex: Int,
        primitiveIndex: Int
    ): CustomMesh {
        val indicesAccessor = primitive.indices?.let { gltfLoaded.model.accessors!![it] }
        val verticesAttributeAccessors = primitive.attributes
            .mapNotNull { p ->
                val accessor = gltfLoaded.model.accessors!![p.value]
                attributeForAccessor(p.key, accessor)?.let { it to accessor }
            }

        return CustomMesh(
            "${declaration.gltfResource}:$meshIndex:$primitiveIndex",
            verticesAttributeAccessors.first().second.count,
            indicesAccessor?.count ?: 0,
            verticesAttributeAccessors.map { it.first },
            false,
            accessorComponentTypeToIndexType(indicesAccessor?.componentType),
            declaration.retentionPolicy
        ) {
            indicesAccessor?.let { indexBytes(getAccessorBytes(it)) }
            verticesAttributeAccessors.forEach {
                attrBytes(it.first, getAccessorBytes(it.second))
            }
        }
    }

    private fun attributeForAccessor(key: String, accessor: Gltf.Accessor): MeshAttribute<*>? {
        val candidates = when (key) {
            "POSITION" -> listOf(POS)
            "NORMAL" -> listOf(NORMAL)
            "TEXCOORD_0" -> listOf(TEX)
            "JOINTS_0" -> listOf(JOINTS_BYTE, JOINTS_SHORT, JOINTS_INT)
            "WEIGHTS_0" -> listOf(WEIGHTS)
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

    private fun getAccessorBytes(accessor: Gltf.Accessor): ByteArray {

        val componentBytes = accessor.componentByteSize()
        val elementComponents = accessor.elementComponentSize()

        val bufferView = gltfLoaded.model.bufferViews!![accessor.bufferView!!]
        val buffer = gltfLoaded.model.buffers!![bufferView.buffer]
        val bufferBytes = getBufferBytes(buffer)
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

    // TODO: redesign; uri is to big for key
    private fun getBufferBytes(buffer: Gltf.Buffer): ByteArray {
        if (buffer.uri == null)
            return gltfLoaded.loadedUris[""]!!
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
                img.uri ?: "${gltfLoaded.id} ${ti.index}", // TODO !!!
                TextureFilter.MipMap,
                TextureWrap.Repeat,
                1024,
                bytes,
                img.mimeType!!.split("/").last(),
                declaration.retentionPolicy
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
        "MAT3" -> 9
        "MAT4" -> 16
        else -> throw KorenderException("GLTF: Not supported accessor type $type")
    }
