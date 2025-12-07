package com.zakgof.korender.gltf

interface GltfModel {
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
