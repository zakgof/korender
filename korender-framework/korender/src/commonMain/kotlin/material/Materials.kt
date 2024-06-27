package com.zakgof.korender.material

import com.zakgof.korender.impl.engine.MaterialDeclaration
import com.zakgof.korender.impl.engine.ShaderDeclaration
import com.zakgof.korender.impl.material.StockUniforms

fun interface MaterialModifier {
    fun applyTo(builder: MaterialBuilder)
}

class MaterialBuilder internal constructor(
    var vertShaderFile: String = "standard.vert",
    var fragShaderFile: String = "standard.frag",
    val options: MutableSet<StandardMaterialOption> = mutableSetOf(),
    val defs: MutableSet<String> = mutableSetOf(),
    val plugins: MutableMap<String, String> = mutableMapOf(),
    var uniforms: UniformSupplier = UniformSupplier { null }
) {
    internal fun toMaterialDeclaration(): MaterialDeclaration = MaterialDeclaration(
        shader = ShaderDeclaration(vertShaderFile, fragShaderFile, defs, options, plugins),
        uniforms = uniforms
    )
}

object MaterialModifiers {
    fun vertex(vertShaderFile: String): MaterialModifier = MaterialModifier { it.vertShaderFile = vertShaderFile }
    fun fragment(fragShaderFile: String): MaterialModifier = MaterialModifier { it.fragShaderFile = fragShaderFile }
    fun options(vararg options: StandardMaterialOption): MaterialModifier = MaterialModifier { it.options += setOf(*options) }
    fun defs(vararg defs: String): MaterialModifier = MaterialModifier { it.defs += setOf(*defs) }
    fun plugin(name: String, shaderFile: String): MaterialModifier = MaterialModifier { it.plugins[name] = shaderFile }
    fun uniforms(uniforms: UniformSupplier): MaterialModifier = MaterialModifier { it.uniforms = uniforms }
    fun standardUniforms(block: StockUniforms.() -> Unit): MaterialModifier = uniforms(StockUniforms().apply(block))
}
