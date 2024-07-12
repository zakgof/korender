package com.zakgof.korender.uniforms

import com.zakgof.korender.math.Color

class FastCloudSkyUniforms : BaseUniformSupplier() {

    var density = 3.0f     // 0..5
    var marble = 2.0f      // 0..5
    var thickness = 10.0f  // 0..20
    var scale = 1.0f       // 0.1..10
    var rippleamount = 0.3f  // 0..1
    var ripplescale = 4.0f  // 1..10
    var darkblue = Color(1f, 0.2f, 0.4f, 0.6f)
    var lightblue = Color(1f, 0.4f, 0.6f, 1.0f)

    override operator fun get(key: String): Any? =
        when (key) {
            "density" -> density
            "thickness" -> thickness
            "scale" -> scale
            "darkblue" -> darkblue
            "lightblue" -> lightblue
            "rippleamount" -> rippleamount
            "ripplescale" -> ripplescale
            else -> super.get(key)
        }
}