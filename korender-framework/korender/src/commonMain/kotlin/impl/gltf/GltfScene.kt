package com.zakgof.korender.impl.gltf

internal class GltfLoaded(
    val model: Gltf,
    val loadedUris: MutableMap<String, ByteArray>
) : AutoCloseable {
    override fun close() {}
}

