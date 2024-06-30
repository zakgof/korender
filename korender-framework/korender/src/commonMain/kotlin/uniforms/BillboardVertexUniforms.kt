package com.zakgof.korender.uniforms

open class BillboardVertexUniforms : BaseUniformSupplier() {

    var xscale = 1f
    var yscale = 1f
    var rotation = 0f

    override operator fun get(key: String): Any? =
        when (key) {
            "xscale" -> xscale
            "yscale" -> yscale
            "rotation" -> rotation
            else -> super.get(key)
        }
}