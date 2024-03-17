package com.zakgof.korender.declaration

import com.zakgof.korender.material.UniformSupplier

data class FilterDeclaration(val fragment: String, val uniforms: UniformSupplier)