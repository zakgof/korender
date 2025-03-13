package com.zakgof.korender

import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Vec3

interface MaterialModifier

interface PostShadingEffect

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

interface WaterParams : BaseParams {
    var waterColor: ColorRGB
    var transparency: Float
    var waveScale: Float
    var waveMagnitude: Float
}

interface AdjustParams : BaseParams {
    var brightness: Float
    var contrast: Float
    var saturation: Float
}

interface StandartParams : BaseParams {

    var baseColor: ColorRGBA
    var baseColorTexture: TextureDeclaration?
    var triplanarScale: Float?

    var normalTexture: TextureDeclaration?
    var shadowTexture: TextureDeclaration?
    var emissiveFactor: ColorRGB
    var emissiveTexture: TextureDeclaration?

    val pbr: Pbr
    val specularGlossiness: SpecularGlossiness

    var xscale: Float
    var yscale: Float
    var rotation: Float

    interface Pbr {
        var metallic: Float
        var roughness: Float
        var metallicRoughnessTexture: TextureDeclaration?
//        var occlusionTexture: TextureDeclaration?
    }

    interface SpecularGlossiness {
        var specularFactor: ColorRGB
        var glossinessFactor: Float
        var specularGlossinessTexture: TextureDeclaration?
    }
}

interface FastCloudSkyParams : BaseParams {
    var density: Float     // 0..5
    var thickness: Float  // 0..20
    var scale: Float       // 0.1..10
    var rippleamount: Float  // 0..1
    var ripplescale: Float // 1..10
    var darkblue: ColorRGB
    var lightblue: ColorRGB
}

interface StarrySkyParams : BaseParams {
    var colorness: Float
    var density: Float
    var speed: Float
    var size: Float
}

interface FogParams : BaseParams {
    var density: Float
    var color: ColorRGB
}

interface SsrParams : BaseParams {
    var maxRayTravel: Float
    var linearSteps: Int
    var binarySteps: Int
    var envTexture: CubeTextureDeclaration?
}

interface BloomParams : BaseParams {
}

interface TerrainParams : BaseParams {
    var heightTexture: TextureDeclaration?
    var heightTextureSize: Int?
    var heightScale: Float
    var terrainCenter: Vec3
    var outsideHeight: Float
}