package com.zakgof.korender.impl.scene

import kotlinx.serialization.Serializable

@Serializable
class SceneModel(
    val textures: Map<String, Texture>,
    val materials: Map<String, Material>,
    val meshes: Map<String, Mesh>,
    val renderables: Map<String, Renderable>,
    val version: Int = 1,
) {
    @Serializable
    class Texture(
        val id: String,
        val format: String,
        val bytes: ByteArray
    )

    @Serializable
    class Material(
        val id: String,
        val baseColor: Long,
        val textureId: String?
    )

    @Serializable
    class Mesh(
        val id: String,
        val vertices: Int,
        val indices: Int,
        val attrBytes: Map<Attribute, ByteArray>,
        val indexBytes: ByteArray?
    )

    @Serializable
    enum class Attribute {
        POS, NORMAL, TEX
    }
    @Serializable
    class Renderable(
        val id: String,
        val meshId: String,
        val materialId: String,
        val transform: FloatArray
    )
}