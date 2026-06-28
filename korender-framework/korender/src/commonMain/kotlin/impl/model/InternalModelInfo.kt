package com.zakgof.korender.impl.model

import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.Mesh
import com.zakgof.korender.ModelInfo
import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform

internal data class InternalModelInfo(
    override val instances: List<Node>,
    override val animations: List<Animation>?,
    override val cameras: List<Camera>?
) : ModelInfo {

    data class Node(
        override val name: String?,
        override val transform: Transform?,
        override val children: List<ModelInfo.Node>?,
        override val renderables: List<Renderable>?
    ) : ModelInfo.Node

    data class Animation(override val name: String?) : ModelInfo.Animation

    data class Camera(
        override val name: String?,
        override val camera: CameraDeclaration,
        override val projection: ProjectionDeclaration
    ) : ModelInfo.Camera

    data class Renderable(
        override val name: String?,
        override val mesh: Mesh,
        override val material: Material?
    ) : ModelInfo.Renderable

    data class Material(
        override val name: String? = null,
        override val color: ColorRGBA = ColorRGBA.White,
        override val colorTextureResource: TextureDeclaration? = null,
        override val metallicFactor: Float = 0.1f,
        override val roughnessFactor: Float = 0.5f,
        override val alphaCutoff: Float = 0.01f,
        override val triplanarScale: Float? = null,
        override val stochasticSharpness: Float? = null,
        override val normalTextureResource: TextureDeclaration? = null,
        override val emission: ColorRGB? = null,
        override val metallicRoughnessTextureResource: TextureDeclaration? = null,
        override val emissionTextureResource: TextureDeclaration? = null,
        override val occlusionTextureResource: TextureDeclaration? = null,
    ) : ModelInfo.Material
}