package com.zakgof.korender

import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Mat4

interface MaterialModifier

interface BaseParams {
    fun set(key: String, value: Any)
}

interface BillboardVertexParams : BaseParams {
    var xscale: Float
    var yscale: Float
    var rotation: Float
}

interface BlurParams : BaseParams {
    var radius: Float
}

interface FireParams : BillboardVertexParams {
    var strength: Float
}

interface FireballParams : BillboardVertexParams {
    var power: Float
}

interface SmokeParams : BillboardVertexParams {
    var density: Float
    var seed: Float
}

interface WaterParams {
    var waterColor: Color
    var transparency: Float
    var waveScale: Float
}

interface AdjustParams {
    var brightness: Float
    var contrast: Float
    var saturation: Float
}

interface StandartParams : BaseParams {

    var noLight: Boolean
    var pcss: Boolean

    var baseColor: Color
    var metallic: Float
    var roughness: Float
    var emissiveFactor: Color

    var albedoTexture: TextureDeclaration?
    var metallicRoughnessTexture: TextureDeclaration?
    var emissiveTexture: TextureDeclaration?
    var occlusionTexture: TextureDeclaration?

    var normalTexture: TextureDeclaration?
    var shadowTexture: TextureDeclaration?

    var jointMatrices: List<Mat4>?
    var inverseBindMatrices: List<Mat4>?

    var xscale: Float
    var yscale: Float
    var rotation: Float
}

interface FastCloudSkyParams {
    var density: Float     // 0..5
    var thickness: Float  // 0..20
    var scale: Float       // 0.1..10
    var rippleamount: Float  // 0..1
    var ripplescale: Float // 1..10
    var darkblue: Color
    var lightblue: Color
}

enum class RenderingOption {

}