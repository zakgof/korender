package com.zakgof.korender.gltf

import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.Mesh
import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.math.Transform

interface GltfUpdate {

    val meshes: List<Mesh>
    val cameras: List<Camera>
    // val root: Node

    interface Camera {
        val name: String?
        val camera: CameraDeclaration
        val projection: ProjectionDeclaration
    }

    interface Node {
        val transform: Transform
        val mesh: com.zakgof.korender.Mesh?
        val children: List<Node>
    }
}