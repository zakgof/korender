package com.zakgof.korender.uniforms

import com.zakgof.korender.material.TextureDeclaration
import com.zakgof.korender.math.Color

class StandartUniforms : BaseUniformSupplier() {

    var baseColor = Color(1f, 0.5f, 0.5f, 0.5f)
    var metallic = 0.3f
    var roughness = 0.3f
    var emissiveFactor = Color(1f, 1f, 1f, 1f)

    var albedoTexture: TextureDeclaration? = null
    var metallicRoughnessTexture: TextureDeclaration? = null
    var emissiveTexture: TextureDeclaration? = null
    var occlusionTexture: TextureDeclaration? = null

    var normalTexture: TextureDeclaration? = null
    var shadowTexture: TextureDeclaration? = null

    // TODO var aperiodicTexture: TextureDeclaration? = null
    // TODO var detailTexture: TextureDeclaration? = null

    // TODO   var triplanarScale = 1.0f
    // TODO   var detailScale = 16.0f
    // TODO   var detailRatio = 0.3f

    var xscale = 1f
    var yscale = 1f
    var rotation = 0f

    override operator fun get(key: String): Any? =
        when (key) {
            "baseColor" -> baseColor
            "metallic" -> metallic
            "roughness" -> roughness
            "emissiveFactor" -> emissiveFactor

            "albedoTexture" -> albedoTexture
            "metallicRoughnessTexture" -> metallicRoughnessTexture
            "emissiveTexture" -> emissiveTexture
            "occlusionTexture" -> occlusionTexture

            "normalTexture" -> normalTexture
            "shadowTexture" -> shadowTexture
            // "detailTexture" -> detailTexture
            // "triplanarScale" -> triplanarScale
            // "detailScale" -> detailScale
            // "detailRatio" -> detailRatio

            "xscale" -> xscale
            "yscale" -> yscale
            "rotation" -> rotation
            else -> super.get(key)
        }
}