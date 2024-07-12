package com.zakgof.korender.uniforms

open class BlurUniforms : BaseUniformSupplier() {

    var radius = 1f

    override operator fun get(key: String): Any? =
        when (key) {
            "radius" -> radius
            else -> super.get(key)
        }
}