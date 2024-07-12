package com.zakgof.korender.uniforms

import com.zakgof.korender.material.TextureDeclaration
import com.zakgof.korender.math.Color

class StandartUniforms : BaseUniformSupplier() {

    var colorTexture: TextureDeclaration? = null
    var normalTexture: TextureDeclaration? = null
    var aperiodicTexture: TextureDeclaration? = null
    var shadowTexture: TextureDeclaration? = null
    var detailTexture: TextureDeclaration? = null

    var color = Color(1f, 0.5f, 0.5f, 0.5f);
    var triplanarScale = 1.0f
    var detailScale = 16.0f
    var detailRatio = 0.3f
    var ambient = 0.3f
    var diffuse = 0.7f
    var specular = 0.3f
    var specularPower = 20f
    var xscale = 1f
    var yscale = 1f
    var rotation = 0f

    override operator fun get(key: String): Any? =
        when (key) {
            "color" -> color
            "colorTexture" -> colorTexture
            "normalTexture" -> normalTexture
            "aperiodicTexture" -> aperiodicTexture
            "shadowTexture" -> shadowTexture
            "detailTexture" -> detailTexture
            "triplanarScale" -> triplanarScale
            "detailScale" -> detailScale
            "detailRatio" -> detailRatio
            "ambient" -> ambient
            "diffuse" -> diffuse
            "specular" -> specular
            "specularPower" -> specularPower
            "xscale" -> xscale
            "yscale" -> yscale
            "rotation" -> rotation
            else -> super.get(key)
        }
}