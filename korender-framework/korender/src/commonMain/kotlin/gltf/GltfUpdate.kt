package com.zakgof.korender.gltf

import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.math.Transform

interface GltfUpdate {

    val animations: List<Animation>
    val cameras: List<Camera>
    val instances: List<Instance>

    interface Animation {
        val name: String?
    }

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
        val name: String?
        val primitives: List<com.zakgof.korender.Mesh>
    }
}