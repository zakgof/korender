package com.zakgof.korender.impl.gltf

import com.zakgof.korender.gltf.GltfModel
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
internal class InternalGltfModel(
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
) : GltfModel {

    @Serializable
    data class Asset(
        override val copyright: String? = null,
        override val generator: String? = null,
        override val version: String,
        override val minVersion: String? = null,
    ) : GltfModel.Asset

    @Serializable
    data class Animation(
        override val channels: List<AnimationChannel>,
        override val samplers: List<AnimationSampler>,
        override val name: String? = null,
    ) : GltfModel.Animation {

        @Serializable
        data class AnimationChannel(
            override val sampler: Int,
            override val target: AnimationChannelTarget,
        ) : GltfModel.Animation.AnimationChannel

        @Serializable
        data class AnimationSampler(
            override val input: Int,
            override val interpolation: String? = null,
            override val output: Int,
        ) : GltfModel.Animation.AnimationSampler

        @Serializable
        data class AnimationChannelTarget(
            override val node: Int? = null,
            override val path: String,
        ) : GltfModel.Animation.AnimationChannelTarget
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
    ) : GltfModel.Accessor {

        @Serializable
        data class Sparse(
            override val count: Int,
            override val indices: SparseIndices,
            override val values: SparseValues,
        ) : GltfModel.Accessor.Sparse

        @Serializable
        data class SparseIndices(
            override val bufferView: Int,
            override val byteOffset: Int = 0,
            override val componentType: Int,
        ) : GltfModel.Accessor.SparseIndices

        @Serializable
        data class SparseValues(
            override val bufferView: Int,
            override val byteOffset: Int = 0,
        ) : GltfModel.Accessor.SparseValues
    }

    @Serializable
    data class Buffer(
        override val uri: String? = null,
        override val byteLength: Int,
        override val name: String? = null,
    ) : GltfModel.Buffer

    @Serializable
    data class BufferView(
        override val buffer: Int,
        override val byteOffset: Int = 0,
        override val byteLength: Int,
        override val byteStride: Int? = null,
        override val target: Int? = null,
        override val name: String? = null,
    ) : GltfModel.BufferView

    @Serializable
    data class Camera(
        override val orthographic: Orthographic? = null,
        override val perspective: Perspective? = null,
        override val type: String,
        override val name: String? = null,
    ) : GltfModel.Camera {

        @Serializable
        data class Orthographic(
            override val xmag: Float,
            override val ymag: Float,
            override val znear: Float,
            override val zfar: Float,
        ) : GltfModel.Camera.Orthographic

        @Serializable
        data class Perspective(
            override val aspectRatio: Float? = null,
            override val yfov: Float,
            override val znear: Float,
            override val zfar: Float? = null,
        ) : GltfModel.Camera.Perspective
    }

    @Serializable
    data class Image(
        override val uri: String? = null,
        override val mimeType: String? = null,
        override val bufferView: Int? = null,
        override val name: String? = null,
    ) : GltfModel.Image

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
    ) : GltfModel.Material {

        @Serializable
        data class PbrMetallicRoughness(
            override val baseColorFactor: List<Float> = listOf(1.0f, 1.0f, 1.0f, 1.0f),
            override val baseColorTexture: TextureInfo? = null,
            override val metallicFactor: Float = 1.0f,
            override val roughnessFactor: Float = 1.0f,
            override val metallicRoughnessTexture: TextureInfo? = null,
        ) : GltfModel.Material.PbrMetallicRoughness

        @Serializable
        data class NormalTextureInfo(
            override val index: Int,
            override val texCoord: Int? = null,
            override val scale: Float = 1.0f,
        ) : GltfModel.Material.NormalTextureInfo

        @Serializable
        data class OcclusionTextureInfo(
            override val index: Int,
            override val texCoord: Int? = null,
            override val strength: Float = 1.0f,
        ) : GltfModel.Material.OcclusionTextureInfo
    }

    @Serializable
    data class Mesh(
        override val name: String? = null,
        override val primitives: List<Primitive>,
        override val weights: List<Float>? = null,
    ) : GltfModel.Mesh {

        @Serializable
        data class Primitive(
            override val attributes: LinkedHashMap<String, Int>,
            override val indices: Int? = null,
            override val material: Int? = null,
            override val mode: Int = 4,
            override val targets: List<Map<String, Int>>? = null,
        ) : GltfModel.Mesh.Primitive
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
    ) : GltfModel.Node

    @Serializable
    data class Sampler(
        override val magFilter: Int? = null,
        override val minFilter: Int? = null,
        override val wrapS: Int? = 10497,
        override val wrapT: Int? = 10497,
        override val name: String? = null,
    ) : GltfModel.Sampler

    @Serializable
    data class Scene(
        override val nodes: List<Int>,
        override val name: String? = null,
    ) : GltfModel.Scene

    @Serializable
    data class Skin(
        override val inverseBindMatrices: Int? = null,
        override val skeleton: Int? = null,
        override val joints: List<Int>,
        override val name: String? = null,
    ) : GltfModel.Skin

    @Serializable
    data class Texture(
        override val sampler: Int? = null,
        override val source: Int? = null,
        override val name: String? = null,
    ) : GltfModel.Texture

    @Serializable
    data class TextureInfo(
        override val index: Int,
        override val texCoord: Int = 0,
    ) : GltfModel.TextureInfo, GltfModel.TextureIndexProvider

    interface TextureIndexProvider : GltfModel.TextureIndexProvider

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
                    Json.decodeFromJsonElement<InternalGltfModel.KHRMaterialsPbrSpecularGlossiness>(value)

                else -> value
            }
        }
        return result
    }

    override fun serialize(encoder: Encoder, value: Map<String, Any>) {
        throw SerializationException("Serialization is not supported for extensions")
    }
}
