package com.zakgof.korender.impl.gltf

internal class GltfLoaded(
    val model: Gltf,
    val id: String,
    val loadedUris: MutableMap<String, ByteArray>
) : AutoCloseable {
    override fun close() {}
}

