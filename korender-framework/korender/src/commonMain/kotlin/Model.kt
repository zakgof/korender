package com.zakgof.korender

import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform

interface ModelInfo {

    val instances: List<Node>
    val animations: List<Animation>?
    val cameras: List<Camera>?

    interface Node {
        val name: String?
        val transform: Transform?
        val children: List<Node>?
        val renderables: List<Renderable>?
    }

    interface Animation {
        val name: String?
    }

    interface Camera {
        val name: String?
        val camera: CameraDeclaration
        val projection: ProjectionDeclaration
    }

    interface Renderable {
        val name: String?
        val mesh: Mesh
        val material: Material?
    }

    interface Material {
        val name: String?
        val color: ColorRGBA
        val colorTextureResource: TextureDeclaration?
        val metallicFactor: Float
        val roughnessFactor: Float
        val alphaCutoff: Float
        val triplanarScale: Float?
        val stochasticSharpness: Float?
        val normalTextureResource: TextureDeclaration?
        val emission: ColorRGB?
        val metallicRoughnessTextureResource: TextureDeclaration?
        val emissionTextureResource: TextureDeclaration?
        val occlusionTextureResource: TextureDeclaration?
    }
}