package com.zakgof.korender.uniforms

class SmokeUniforms : BillboardVertexUniforms() {

    var density = 0.5f
    var seed = 0f

    override operator fun get(key: String): Any? =
        when (key) {
            "density" -> density
            "seed" -> seed
            else -> super.get(key)
        }
}