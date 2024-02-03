package com.zakgof.korender

fun interface UniformSupplier {
    operator fun get(key: String): Any?
}
