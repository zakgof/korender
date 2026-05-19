package com.zakgof.korender

import com.zakgof.korender.math.Transform

interface ModelInfo {

    val instances: List<Node>
    val animations: List<Animation>?
    val cameras: List<Camera>?

    interface Node {
        val transform: Transform?
        val children: List<Node>?
        val name: String?
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