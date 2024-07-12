package com.zakgof.korender.uniforms


class AdjustUniforms : BaseUniformSupplier() {

    var brightness: Float = 0f;
    var contrast: Float = 1f
    var saturation: Float = 1f

    override operator fun get(key: String): Any? =
        when (key) {
            "brightness" -> brightness
            "contrast" -> contrast
            "saturation" -> saturation
            else -> super.get(key)
        }
}