package com.zakgof.korender.uniforms

fun interface UniformSupplier {
    operator fun get(key: String): Any?
    operator fun plus(that: UniformSupplier) = UniformSupplier { this[it] ?: that[it] }
    operator fun plus(that: Map<String, Any?>) = UniformSupplier { this[it] ?: that[it] }
}
