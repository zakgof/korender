package com.zakgof.korender.uniforms

import com.zakgof.korender.math.Color

class FastCloudSkyUniforms : BaseUniformSupplier() {

    var density = 1.0f
    var marble1 = 2.0f
    var marble2 = 2.0f
    var scale = 1.0f
    var darkblue = Color(1f, 0.2f, 0.4f, 0.6f)
    var lightblue = Color(1f, 0.4f, 0.6f, 1.0f)

    override operator fun get(key: String): Any? =
        when (key) {
            "density" -> density
            "marble1" -> marble1
            "marble2" -> marble2
            "scale" -> scale
            "darkblue" -> darkblue
            "lightblue" -> lightblue
            else -> super.get(key)
        }
}