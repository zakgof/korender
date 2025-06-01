package com.zakgof.korender.impl.gltf

import com.zakgof.korender.math.Mat4

internal class GltfLoaded(
    val model: Gltf,
    val id: String,
    val loadedUris: MutableMap<String, ByteArray>,
    val loadedAccessors: Map<Int, ByteArray>,
    val loadedSkins: Map<Int, List<Mat4>>
) : AutoCloseable {
    override fun close() {}
}

