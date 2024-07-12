package com.zakgof.korender.uniforms

class FireUniforms : BillboardVertexUniforms() {

    var strength = 3.0f

    override operator fun get(key: String): Any? =
        if (key == "strength") strength else super.get(key)
}