package com.zakgof.korender.material

class DynamicUniformSupplier(private val key: String, private val block: () -> Any) : UniformSupplier {

    override fun get(key: String): Any? = if (key == this.key) block() else null

}
