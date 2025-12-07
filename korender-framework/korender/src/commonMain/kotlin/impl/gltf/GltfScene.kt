package com.zakgof.korender.impl.gltf

import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.Mesh
import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.gltf.GltfUpdate
import com.zakgof.korender.math.Mat4

internal class GltfLoaded(
    val model: InternalGltfModel,
    val id: String,
    val loadedUris: MutableMap<String, ByteArray>,
    val loadedAccessors: AccessorCache,
    val loadedSkins: Map<Int, List<Mat4>>
) : AutoCloseable {
    override fun close() {}
}

internal class AccessorCache(
    val all: Map<Int, ByteArray>,
    val floats: Map<Int, FloatArray>,
    val floatArrays: MutableMap<Int, Array<List<Float>>>
)

internal class InternalUpdateData(override val cameras: List<InternalGltfCamera>, override val meshes: List<Mesh>) : GltfUpdate {
    class InternalGltfCamera(
        override val name: String?,
        override val camera: CameraDeclaration,
        override val projection: ProjectionDeclaration
    ) : GltfUpdate.Camera
}
