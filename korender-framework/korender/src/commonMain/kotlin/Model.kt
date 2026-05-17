package com.zakgof.korender

import com.zakgof.korender.math.Transform

interface ModelInfo {

    val instances: List<Instance>
    val animations: List<Animation>
    val cameras: List<Camera>

    interface Instance {
        val rootNode: Node
    }

    interface Node {
        val transform: Transform
        val children: List<Node>
        val mesh: Mesh?
    }

    interface Animation {
        val name: String?
    }

    interface Camera {
        val name: String?
        val camera: CameraDeclaration
        val projection: ProjectionDeclaration
    }
}