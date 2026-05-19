package com.zakgof.korender.impl.prefab

import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.Mesh
import com.zakgof.korender.ModelInfo
import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.math.Transform

class InternalModelInfo(
    override val instances: List<Node>,
    override val animations: List<Animation>?,
    override val cameras: List<Camera>?
) : ModelInfo {

    class Node(
        override val transform: Transform?,
        override val children: List<ModelInfo.Node>?,
        override val name: String?,
        override val mesh: Mesh?
    ) : ModelInfo.Node

    class Animation(override val name: String?) : ModelInfo.Animation

    class Camera(
        override val name: String?,
        override val camera: CameraDeclaration,
        override val projection: ProjectionDeclaration
    ) : ModelInfo.Camera
}