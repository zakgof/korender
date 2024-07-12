package com.zakgof.korender.uniforms

open class BaseUniformSupplier : UniformSupplier {

    private val static = mutableMapOf<String, Any>()
    private val dynamic = mutableMapOf<String, () -> Any>()

    fun static(key: String, value: Any) {
        static[key] = value
    }

    fun dynamic(key: String, valueSupplier: () -> Any) {
        dynamic[key] = valueSupplier
    }

    override operator fun get(key: String): Any? = static[key] ?: dynamic[key]?.let { it() }
}