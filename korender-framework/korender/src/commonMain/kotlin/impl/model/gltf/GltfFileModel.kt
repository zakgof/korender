package com.zakgof.korender.impl.model.gltf

import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.mapSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
internal class InternalGltfFileModel(
    override val scene: Int,
    override val asset: Asset,
    override val accessors: List<Accessor>? = null,
    override val animations: List<Animation>? = null,
    override val buffers: List<Buffer>? = null,
    override val bufferViews: List<BufferView>? = null,
    override val cameras: List<Camera>? = null,
    override val images: List<Image>? = null,
    override val materials: List<Material>? = null,
    override val meshes: List<Mesh>? = null,
    override val nodes: List<Node>? = null,
    override val samplers: List<Sampler>? = null,
    override val scenes: List<Scene>? = null,
    override val skins: List<Skin>? = null,
    override val textures: List<Texture>? = null,
) : GltfFileModel {

    @Serializable
    data class Asset(
        override val copyright: String? = null,
        override val generator: String? = null,
        override val version: String,
        override val minVersion: String? = null,
    ) : GltfFileModel.Asset

    @Serializable
    data class Animation(
        override val channels: List<AnimationChannel>,
        override val samplers: List<AnimationSampler>,
        override val name: String? = null,
    ) : GltfFileModel.Animation {

        @Serializable
        data class AnimationChannel(
            override val sampler: Int,
            override val target: AnimationChannelTarget,
        ) : GltfFileModel.Animation.AnimationChannel

        @Serializable
        data class AnimationSampler(
            override val input: Int,
            override val interpolation: String? = null,
            override val output: Int,
        ) : GltfFileModel.Animation.AnimationSampler

        @Serializable
        data class AnimationChannelTarget(
            override val node: Int? = null,
            override val path: String,
        ) : GltfFileModel.Animation.AnimationChannelTarget
    }

    @Serializable
    data class Accessor(
        override val bufferView: Int? = null,
        override val byteOffset: Int? = 0,
        override val componentType: Int,
        override val normalized: Boolean = false,
        override val count: Int,
        override val type: String,
        override val max: List<Double>? = null,
        override val min: List<Double>? = null,
        override val sparse: Sparse? = null,
        override val name: String? = null,
    ) : GltfFileModel.Accessor {

        @Serializable
        data class Sparse(
            override val count: Int,
            override val indices: SparseIndices,
            override val values: SparseValues,
        ) : GltfFileModel.Accessor.Sparse

        @Serializable
        data class SparseIndices(
            override val bufferView: Int,
            override val byteOffset: Int = 0,
            override val componentType: Int,
        ) : GltfFileModel.Accessor.SparseIndices

        @Serializable
        data class SparseValues(
            override val bufferView: Int,
            override val byteOffset: Int = 0,
        ) : GltfFileModel.Accessor.SparseValues
    }

    @Serializable
    data class Buffer(
        override val uri: String? = null,
        override val byteLength: Int,
        override val name: String? = null,
    ) : GltfFileModel.Buffer

    @Serializable
    data class BufferView(
        override val buffer: Int,
        override val byteOffset: Int = 0,
        override val byteLength: Int,
        override val byteStride: Int? = null,
        override val target: Int? = null,
        override val name: String? = null,
    ) : GltfFileModel.BufferView

    @Serializable
    data class Camera(
        override val orthographic: Orthographic? = null,
        override val perspective: Perspective? = null,
        override val type: String,
        override val name: String? = null,
    ) : GltfFileModel.Camera {

        @Serializable
        data class Orthographic(
            override val xmag: Float,
            override val ymag: Float,
            override val znear: Float,
            override val zfar: Float,
        ) : GltfFileModel.Camera.Orthographic

        @Serializable
        data class Perspective(
            override val aspectRatio: Float? = null,
            override val yfov: Float,
            override val znear: Float,
            override val zfar: Float? = null,
        ) : GltfFileModel.Camera.Perspective
    }

    @Serializable
    data class Image(
        override val uri: String? = null,
        override val mimeType: String? = null,
        override val bufferView: Int? = null,
        override val name: String? = null,
    ) : GltfFileModel.Image

    @Serializable
    data class Material(
        override val name: String? = null,
        override val pbrMetallicRoughness: PbrMetallicRoughness? = null,
        override val normalTexture: NormalTextureInfo? = null,
        override val occlusionTexture: OcclusionTextureInfo? = null,
        override val emissiveTexture: TextureInfo? = null,
        override val emissiveFactor: List<Float>? = listOf(0.0f, 0.0f, 0.0f),
        override val alphaMode: String? = "OPAQUE",
        override val alphaCutoff: Float? = 0.5f,
        override val doubleSided: Boolean? = false,
        @Serializable(with = ExtensionsDeserializer::class)
        override val extensions: Map<String, @Contextual Any>? = null,
    ) : GltfFileModel.Material {

        @Serializable
        data class PbrMetallicRoughness(
            override val baseColorFactor: List<Float> = listOf(1.0f, 1.0f, 1.0f, 1.0f),
            override val baseColorTexture: TextureInfo? = null,
            override val metallicFactor: Float = 1.0f,
            override val roughnessFactor: Float = 1.0f,
            override val metallicRoughnessTexture: TextureInfo? = null,
        ) : GltfFileModel.Material.PbrMetallicRoughness

        @Serializable
        data class NormalTextureInfo(
            override val index: Int,
            override val texCoord: Int? = null,
            override val scale: Float = 1.0f,
        ) : GltfFileModel.Material.NormalTextureInfo

        @Serializable
        data class OcclusionTextureInfo(
            override val index: Int,
            override val texCoord: Int? = null,
            override val strength: Float = 1.0f,
        ) : GltfFileModel.Material.OcclusionTextureInfo
    }

    @Serializable
    data class Mesh(
        override val name: String? = null,
        override val primitives: List<Primitive>,
        override val weights: List<Float>? = null,
    ) : GltfFileModel.Mesh {

        @Serializable
        data class Primitive(
            override val attributes: LinkedHashMap<String, Int>,
            override val indices: Int? = null,
            override val material: Int? = null,
            override val mode: Int = 4,
            override val targets: List<Map<String, Int>>? = null,
        ) : GltfFileModel.Mesh.Primitive
    }

    @Serializable
    data class Node(
        override val camera: Int? = null,
        override val children: List<Int>? = null,
        override val skin: Int? = null,
        override val matrix: List<Float>? = null,
        override val mesh: Int? = null,
        override val rotation: List<Float>? = null,
        override val scale: List<Float>? = null,
        override val translation: List<Float>? = null,
        override val weights: List<Float>? = null,
        override val name: String? = null,
    ) : GltfFileModel.Node

    @Serializable
    data class Sampler(
        override val magFilter: Int? = null,
        override val minFilter: Int? = null,
        override val wrapS: Int? = 10497,
        override val wrapT: Int? = 10497,
        override val name: String? = null,
    ) : GltfFileModel.Sampler

    @Serializable
    data class Scene(
        override val nodes: List<Int>,
        override val name: String? = null,
    ) : GltfFileModel.Scene

    @Serializable
    data class Skin(
        override val inverseBindMatrices: Int? = null,
        override val skeleton: Int? = null,
        override val joints: List<Int>,
        override val name: String? = null,
    ) : GltfFileModel.Skin

    @Serializable
    data class Texture(
        override val sampler: Int? = null,
        override val source: Int? = null,
        override val name: String? = null,
    ) : GltfFileModel.Texture

    @Serializable
    data class TextureInfo(
        override val index: Int,
        override val texCoord: Int = 0,
    ) : GltfFileModel.TextureInfo, GltfFileModel.TextureIndexProvider

    interface TextureIndexProvider : GltfFileModel.TextureIndexProvider

    @Serializable
    data class KHRMaterialsPbrSpecularGlossiness(
        val diffuseFactor: List<Float> = listOf(1.0f, 1.0f, 1.0f, 1.0f),
        val diffuseTexture: TextureInfo? = null,
        val specularFactor: List<Float> = listOf(1.0f, 1.0f, 1.0f),
        val glossinessFactor: Float = 1.0f,
        val specularGlossinessTexture: TextureInfo? = null,
    )
}

object ExtensionsDeserializer : KSerializer<Map<String, Any>> {
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor = mapSerialDescriptor(
        PrimitiveSerialDescriptor("key", PrimitiveKind.STRING),
        JsonElement.serializer().descriptor
    )

    override fun deserialize(decoder: Decoder): Map<String, Any> {
        val jsonDecoder = decoder as JsonDecoder
        val jsonObject = jsonDecoder.decodeJsonElement() as? JsonObject
            ?: throw SerializationException("Expected JsonObject for extensions")

        val result = mutableMapOf<String, Any>()
        jsonObject.forEach { (key, value) ->
            result[key] = when (key) {
                "KHR_materials_pbrSpecularGlossiness" ->
                    Json.decodeFromJsonElement<InternalGltfFileModel.KHRMaterialsPbrSpecularGlossiness>(value)

                else -> value
            }
        }
        return result
    }

    override fun serialize(encoder: Encoder, value: Map<String, Any>) {
        throw SerializationException("Serialization is not supported for extensions")
    }
}

interface GltfFileModel {
    val scene: Int
    val asset: Asset
    val accessors: List<Accessor>?
    val animations: List<Animation>?
    val buffers: List<Buffer>?
    val bufferViews: List<BufferView>?
    val cameras: List<Camera>?
    val images: List<Image>?
    val materials: List<Material>?
    val meshes: List<Mesh>?
    val nodes: List<Node>?
    val samplers: List<Sampler>?
    val scenes: List<Scene>?
    val skins: List<Skin>?
    val textures: List<Texture>?

    interface Asset {
        val copyright: String?
        val generator: String?
        val version: String
        val minVersion: String?
    }

    interface Animation {
        val channels: List<AnimationChannel>
        val samplers: List<AnimationSampler>
        val name: String?

        interface AnimationChannel {
            val sampler: Int
            val target: AnimationChannelTarget
        }

        interface AnimationSampler {
            val input: Int
            val interpolation: String?
            val output: Int
        }

        interface AnimationChannelTarget {
            val node: Int?
            val path: String
        }
    }

    interface Accessor {
        val bufferView: Int?
        val byteOffset: Int?
        val componentType: Int
        val normalized: Boolean
        val count: Int
        val type: String
        val max: List<Double>?
        val min: List<Double>?
        val sparse: Sparse?
        val name: String?

        interface Sparse {
            val count: Int
            val indices: SparseIndices
            val values: SparseValues
        }

        interface SparseIndices {
            val bufferView: Int
            val byteOffset: Int
            val componentType: Int
        }

        interface SparseValues {
            val bufferView: Int
            val byteOffset: Int
        }
    }

    interface Buffer {
        val uri: String?
        val byteLength: Int
        val name: String?
    }

    interface BufferView {
        val buffer: Int
        val byteOffset: Int
        val byteLength: Int
        val byteStride: Int?
        val target: Int?
        val name: String?
    }

    interface Camera {
        val orthographic: Orthographic?
        val perspective: Perspective?
        val type: String
        val name: String?

        interface Orthographic {
            val xmag: Float
            val ymag: Float
            val znear: Float
            val zfar: Float
        }

        interface Perspective {
            val aspectRatio: Float?
            val yfov: Float
            val znear: Float
            val zfar: Float?
        }
    }

    interface Image {
        val uri: String?
        val mimeType: String?
        val bufferView: Int?
        val name: String?
    }

    interface Material {
        val name: String?
        val pbrMetallicRoughness: PbrMetallicRoughness?
        val normalTexture: NormalTextureInfo?
        val occlusionTexture: OcclusionTextureInfo?
        val emissiveTexture: TextureInfo?
        val emissiveFactor: List<Float>?
        val alphaMode: String?
        val alphaCutoff: Float?
        val doubleSided: Boolean?
        val extensions: Map<String, Any>?

        interface PbrMetallicRoughness {
            val baseColorFactor: List<Float>
            val baseColorTexture: TextureInfo?
            val metallicFactor: Float
            val roughnessFactor: Float
            val metallicRoughnessTexture: TextureInfo?
        }

        interface NormalTextureInfo : TextureIndexProvider {
            val texCoord: Int?
            val scale: Float
        }

        interface OcclusionTextureInfo : TextureIndexProvider {
            val texCoord: Int?
            val strength: Float
        }
    }

    interface Mesh {
        val name: String?
        val primitives: List<Primitive>
        val weights: List<Float>?

        interface Primitive {
            val attributes: Map<String, Int>
            val indices: Int?
            val material: Int?
            val mode: Int
            val targets: List<Map<String, Int>>?
        }
    }

    interface Node {
        val camera: Int?
        val children: List<Int>?
        val skin: Int?
        val matrix: List<Float>?
        val mesh: Int?
        val rotation: List<Float>?
        val scale: List<Float>?
        val translation: List<Float>?
        val weights: List<Float>?
        val name: String?
    }

    interface Sampler {
        val magFilter: Int?
        val minFilter: Int?
        val wrapS: Int?
        val wrapT: Int?
        val name: String?
    }

    interface Scene {
        val nodes: List<Int>
        val name: String?
    }

    interface Skin {
        val inverseBindMatrices: Int?
        val skeleton: Int?
        val joints: List<Int>
        val name: String?
    }

    interface Texture {
        val sampler: Int?
        val source: Int?
        val name: String?
    }

    interface TextureInfo : TextureIndexProvider {
        val texCoord: Int
    }

    interface TextureIndexProvider {
        val index: Int
    }
}