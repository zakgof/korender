package com.zakgof.korender.gltf

import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.math.Transform

interface GltfUpdate {

    val meshes: List<Mesh>
    val cameras: List<Camera>
    val instances: List<Instance>

    interface Camera {
        val name: String?
        val camera: CameraDeclaration
        val projection: ProjectionDeclaration
    }

    interface Instance {
        val rootNode: Node
    }

    interface Node {
        val transform: Transform
        val mesh: Mesh?
        val children: List<Node>
    }

    interface Mesh {
        val primitives: List<com.zakgof.korender.Mesh>
    }
}