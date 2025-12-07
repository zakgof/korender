package com.zakgof.korender.impl.gltf

import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.Mesh
import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.gltf.GltfUpdate
import com.zakgof.korender.impl.geometry.CMesh
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Transform

internal class GltfLoaded(
    val model: InternalGltfModel,
    val id: String,
    val loadedUris: MutableMap<String, ByteArray>,
    val loadedAccessors: AccessorCache,
    val loadedSkins: Map<Int, List<Mat4>>,
) : AutoCloseable {
    val loadedMeshes = mutableMapOf<Int, CMesh>()
    override fun close() {}
}

internal class AccessorCache(
    val all: Map<Int, ByteArray>,
    val floats: Map<Int, FloatArray>,
    val floatArrays: MutableMap<Int, Array<List<Float>>>,
)

internal class InternalUpdateData(override val cameras: List<InternalGltfCamera>, override val meshes: List<Mesh>, override val instances: List<GltfUpdate.Instance>) : GltfUpdate {
    class InternalGltfCamera(
        override val name: String?,
        override val camera: CameraDeclaration,
        override val projection: ProjectionDeclaration,
    ) : GltfUpdate.Camera

    class Instance(override val rootNode: Node): GltfUpdate.Instance

    class Node(override val transform: Transform, override val mesh: Mesh?, override val children: List<Node>) : GltfUpdate.Node
}
