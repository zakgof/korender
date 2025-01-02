package com.zakgof.korender

import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Mat4List

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

    var pcss: Boolean

    var baseColor: Color
    var baseColorTexture: TextureDeclaration?

    val pbr: Pbr
    val specularGlossiness: SpecularGlossiness

    var normalTexture: TextureDeclaration?
    var shadowTexture: TextureDeclaration?

    var jointMatrices: Mat4List?
    var inverseBindMatrices: Mat4List?

    var xscale: Float
    var yscale: Float
    var rotation: Float

    interface Pbr {
        var metallic: Float
        var roughness: Float
        var emissiveFactor: Color
        var metallicRoughnessTexture: TextureDeclaration?
        var emissiveTexture: TextureDeclaration?
        var occlusionTexture: TextureDeclaration?
    }

    interface SpecularGlossiness {
        var specularFactor: Color
        var glossinessFactor: Float
        var specularGlossinessTexture: TextureDeclaration?
    }
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