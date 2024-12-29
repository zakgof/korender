package com.zakgof.korender.impl.material

internal class CombinedUniformSupplier(private vararg val uniformSuppliers: UniformSupplier) : UniformSupplier {
    override fun update() = uniformSuppliers.forEach { it.update() }
    override operator fun get(key: String) = uniformSuppliers.firstNotNullOfOrNull { it[key] }
}
