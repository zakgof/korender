package com.zakgof.korender.impl.material

internal class MapUniformSupplier(private val map: Map<String, Any?>) : UniformSupplier {

    constructor(vararg pairs: Pair<String, Any?>) : this(mapOf(*pairs))

    override fun update() {}

    override fun get(key: String): Any? = map[key]
}
