package com.zakgof.korender.impl.material

internal interface UniformSupplier {
    fun update()
    operator fun get(key: String): Any?
}
