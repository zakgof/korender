package com.zakgof.korender.material

fun interface UniformSupplier {
    operator fun get(key: String): Any?
    operator fun plus(that: UniformSupplier) = UniformSupplier { this[it] ?: that[it] }

    fun boil(loader: (Any) -> Any) {}
}