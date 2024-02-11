package com.zakgof.korender.material

fun interface UniformSupplier {
    operator fun get(key: String): Any?
}
