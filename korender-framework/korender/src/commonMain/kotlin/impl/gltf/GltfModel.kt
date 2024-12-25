package com.zakgof.korender.impl.gltf

import kotlinx.serialization.Serializable

@Serializable
class Gltf(
    val scene: Int,
    val asset: Asset,
    val accessors: List<Accessor>? = null,
    val buffers: List<Buffer>? = null,
    val bufferViews: List<BufferView>? = null,
    val cameras: List<Camera>? = null,
    val images: List<Image>? = null,
    val materials: List<Material>? = null,
    val meshes: List<Mesh>? = null,
    val nodes: List<Node>? = null,
    val samplers: List<Sampler>? = null,
    val scenes: List<Scene>? = null,
    val textures: List<Texture>? = null
) {

    @Serializable
    class Asset(
        val copyright: String? = null,
        val generator: String? = null,
        val version: String,
        val minVersion: String? = null
    )

    @Serializable
    data class Accessor(
        val bufferView: Int? = null,
        val byteOffset: Int? = 0,
        val componentType: Int,
        val normalized: Boolean = false,
        val count: Int,
        val type: String,
        val max: List<Double>? = null,
        val min: List<Double>? = null,
        val sparse: Sparse? = null,
        val name: String? = null,
    ) {
        @Serializable
        data class Sparse(
            val count: Int,
            val indices: SparseIndices,
            val values: SparseValues
        )

        @Serializable
        data class SparseIndices(
            val bufferView: Int,
            val byteOffset: Int = 0,
            val componentType: Int
        )

        @Serializable
        data class SparseValues(
            val bufferView: Int,
            val byteOffset: Int = 0
        )
    }

    @Serializable
    data class Buffer(
        val uri: String? = null,
        val byteLength: Int,
        val name: String? = null,
    )

    @Serializable
    data class BufferView(
        val buffer: Int,
        val byteOffset: Int = 0,
        val byteLength: Int,
        val byteStride: Int? = null,
        val target: Int? = null,
        val name: String? = null,
    )

    @Serializable
    data class Camera(
        val orthographic: Orthographic? = null,
        val perspective: Perspective? = null,
        val type: String,
        val name: String? = null,
    ) {

        @Serializable
        data class Orthographic(
            val xMag: Float,
            val yMag: Float,
            val zNear: Float,
            val zFar: Float
        )

        @Serializable
        data class Perspective(
            val aspectRatio: Float? = null,
            val yfov: Float,
            val zNear: Float,
            val zFar: Float? = null
        )
    }

    @Serializable
    data class Image(
        val uri: String? = null,
        val mimeType: String? = null,
        val bufferView: Int? = null,
        val name: String? = null
    )

    @Serializable
    data class Material(
        val name: String? = null,
        val pbrMetallicRoughness: PbrMetallicRoughness? = null,
        val normalTexture: NormalTextureInfo? = null,
        val occlusionTexture: OcclusionTextureInfo? = null,
        val emissiveTexture: TextureInfo? = null,
        val emissiveFactor: List<Float>? = listOf(0.0f, 0.0f, 0.0f),
        val alphaMode: String? = "OPAQUE",
        val alphaCutoff: Float? = 0.5f,
        val doubleSided: Boolean? = false
    ) {
        @Serializable
        data class PbrMetallicRoughness(
            val baseColorFactor: List<Float> = listOf(1.0f, 1.0f, 1.0f, 1.0f),
            val baseColorTexture: TextureInfo? = null,
            val metallicFactor: Float = 1.0f,
            val roughnessFactor: Float = 1.0f,
            val metallicRoughnessTexture: TextureInfo? = null
        )

        @Serializable
        data class NormalTextureInfo(
            val index: Int,
            val texCoord: Int? = null,
            val scale: Float = 1.0f
        )

        @Serializable
        data class OcclusionTextureInfo(
            val index: Int,
            val texCoord: Int? = null,
            val strength: Float = 1.0f,
        )
    }

    @Serializable
    data class Mesh(
        val name: String? = null,
        val primitives: List<Primitive>,
        val weights: List<Float>? = null
    ) {

        @Serializable
        data class Primitive(
            val attributes: LinkedHashMap<String, Int>,
            val indices: Int? = null,
            val material: Int? = null,
            val mode: Int = 4,
            val targets: List<Map<String, Int>>? = null,
        )

    }

    @Serializable
    data class Node(
        val camera: Int? = null,
        val children: List<Int>? = null,
        val skin: Int? = null,
        val matrix: List<Float>? = listOf(
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
        ),
        val mesh: Int? = null,
        val rotation: List<Float>? = listOf(0.0f, 0.0f, 0.0f, 1.0f),
        val scale: List<Float>? = listOf(1.0f, 1.0f, 1.0f),
        val translation: List<Float>? = listOf(0.0f, 0.0f, 0.0f),
        val weights: List<Float>? = null,
        val name: String? = null
    )

    @Serializable
    data class Sampler(
        val magFilter: Int? = null,
        val minFilter: Int? = null,
        val wrapS: Int? = 10497,
        val wrapT: Int? = 10497,
        val name: String? = null
    )

    @Serializable
    data class Scene(
        val nodes: List<Int>,
        val name: String? = null
    )

    @Serializable
    data class Texture(
        val sampler: Int? = null,
        val source: Int? = null,
        val name: String? = null
    )

    @Serializable
    data class TextureInfo(
        val index: Int,
        val texCoord: Int = 0
    )


}

