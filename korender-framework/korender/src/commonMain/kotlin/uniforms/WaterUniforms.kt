package com.zakgof.korender.uniforms

import com.zakgof.korender.math.Color

class WaterUniforms : BaseUniformSupplier() {

    var waterColor: Color = Color(1.0f, 0.1f, 0.2f, 0.3f)
    var transparency: Float = 0.1f
    var waveScale: Float = 0.04f

    override operator fun get(key: String): Any? =
        when (key) {
            "waterColor" -> waterColor
            "transparency" -> transparency
            "waveScale" -> waveScale
            else -> super.get(key)
        }
}