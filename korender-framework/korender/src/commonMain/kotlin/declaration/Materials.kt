package com.zakgof.korender.declaration

import com.zakgof.korender.impl.engine.ShaderDeclaration
import com.zakgof.korender.impl.material.StockUniforms

object Materials {
    fun standard(vararg options: StandardMaterialOption, plugins: Map<String, String> = mapOf(), block: StockUniforms.() -> Unit) =
        MaterialDeclaration(
            ShaderDeclaration("standard.vert", "standard.frag", options, plugins),
            StockUniforms().apply(block)
        )

    fun custom(vertexFile: String, fragFile: String, vararg defs: String, plugins: Map<String, String> = mapOf(), uniforms: UniformSupplier = UniformSupplier { null }) =
        MaterialDeclaration(
            ShaderDeclaration(vertexFile, fragFile, setOf(*defs), plugins),
            uniforms
        )

    fun billboardStandard(fragFile: String = "standard.frag", vararg options: StandardMaterialOption, plugins: Map<String, String> = mapOf(), block: StockUniforms.() -> Unit) =
        BillboardMaterialDeclaration(
            ShaderDeclaration("billboard.vert", fragFile, options, plugins),
            StockUniforms().apply(block)
        )

    fun billboardCustom(fragFile: String, vararg defs: String, plugins: Map<String, String> = mapOf(), uniforms: UniformSupplier = UniformSupplier { null }) =
        BillboardMaterialDeclaration(
            ShaderDeclaration("billboard.vert", fragFile, setOf(*defs), plugins),
            uniforms
        )
}

class MaterialDeclaration internal constructor(internal val shader: ShaderDeclaration, internal val uniforms: UniformSupplier)

class BillboardMaterialDeclaration internal constructor(internal val shader: ShaderDeclaration, internal val uniforms: UniformSupplier)
class FilterDeclaration internal constructor(internal val shader: ShaderDeclaration, internal val uniforms: UniformSupplier)

