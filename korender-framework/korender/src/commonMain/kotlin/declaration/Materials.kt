package com.zakgof.korender.declaration

import com.zakgof.korender.impl.engine.CustomShaderDeclaration
import com.zakgof.korender.impl.engine.ShaderDeclaration
import com.zakgof.korender.impl.engine.StandardShaderDeclaration
import com.zakgof.korender.impl.material.StockUniforms
import java.util.EnumSet

object Materials {
    fun standard(vararg options: StandardMaterialOption, block: StockUniforms.() -> Unit) =
        MaterialDeclaration(
            StandardShaderDeclaration(if (options.isEmpty()) EnumSet.noneOf(StandardMaterialOption::class.java) else EnumSet.of(options[0], *options)),
            StockUniforms().apply(block)
        )

    fun custom(vertexFile: String, fragFile: String, vararg defs: String, uniforms: UniformSupplier = UniformSupplier { null }) =
        MaterialDeclaration(
            CustomShaderDeclaration(vertexFile, fragFile, setOf(*defs)),
            uniforms
        )
}

class MaterialDeclaration internal constructor(internal val shader: ShaderDeclaration, internal val uniforms: UniformSupplier)