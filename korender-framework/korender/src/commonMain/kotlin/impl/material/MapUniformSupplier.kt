package com.zakgof.korender.impl.material

import com.zakgof.korender.material.UniformSupplier

class MapUniformSupplier(private val map: Map<String, Any?>) : UniformSupplier {
    constructor(vararg pairs: Pair<String, Any?>) : this(mapOf(*pairs))

    override fun get(key: String): Any? = map[key]
}
