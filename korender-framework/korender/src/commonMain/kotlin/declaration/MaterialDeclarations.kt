package com.zakgof.korender.declaration

import com.zakgof.korender.material.StockUniforms
import com.zakgof.korender.material.UniformSupplier

object MaterialDeclarations {
    fun standard(vararg defs: String, block: StockUniforms.() -> Unit) =
        MaterialDeclaration(
            ShaderDeclaration("standard.vert", "standard.frag", setOf(*defs)),
            StockUniforms().apply(block)
        )
}

class MaterialDeclaration(
    val shader: ShaderDeclaration,
    val uniforms: UniformSupplier
)