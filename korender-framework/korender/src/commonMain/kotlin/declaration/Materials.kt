package com.zakgof.korender.declaration

import com.zakgof.korender.impl.engine.ShaderDeclaration
import com.zakgof.korender.impl.material.StockUniforms

object Materials {
    fun standard(vararg defs: String, block: StockUniforms.() -> Unit) =
        MaterialDeclaration(
            ShaderDeclaration("standard.vert", "standard.frag", setOf(*defs)),
            StockUniforms().apply(block)
        )

    fun custom(vertexFile: String, fragFile: String, vararg defs: String, uniforms: UniformSupplier = UniformSupplier { null }) =
        MaterialDeclaration(
            ShaderDeclaration(vertexFile, fragFile, setOf(*defs)),
            uniforms
        )
}

class MaterialDeclaration internal constructor(internal val shader: ShaderDeclaration, internal val uniforms: UniformSupplier)