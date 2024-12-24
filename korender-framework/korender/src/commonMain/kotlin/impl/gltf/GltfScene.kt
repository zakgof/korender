package com.zakgof.korender.impl.gltf

internal class GltfLoaded(
    val model: Gltf,
    val loadedUris: Map<String, ByteArray>
) : AutoCloseable {
    override fun close() {}
}

internal class GltfScene