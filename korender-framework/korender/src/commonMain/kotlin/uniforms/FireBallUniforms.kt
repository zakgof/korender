package com.zakgof.korender.uniforms

class FireBallUniforms : BillboardVertexUniforms() {

    var power = 0.5f

    override operator fun get(key: String): Any? =
        if (key == "power") power else super.get(key)
}