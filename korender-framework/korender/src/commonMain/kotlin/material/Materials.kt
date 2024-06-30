package com.zakgof.korender.material

import com.zakgof.korender.impl.engine.MaterialDeclaration
import com.zakgof.korender.impl.engine.ShaderDeclaration
import com.zakgof.korender.uniforms.StandartUniforms
import com.zakgof.korender.uniforms.UniformSupplier

fun interface MaterialModifier {
    fun applyTo(builder: MaterialBuilder)
}

class MaterialBuilder internal constructor(
    var vertShaderFile: String = "standart.vert",
    var fragShaderFile: String = "standart.frag",
    val options: MutableSet<StandartMaterialOption> = mutableSetOf(),
    val defs: MutableSet<String> = mutableSetOf(),
    val plugins: MutableMap<String, String> = mutableMapOf(),
    var uniforms: UniformSupplier = UniformSupplier { null }
) {
    internal fun toMaterialDeclaration(): MaterialDeclaration = MaterialDeclaration(
        shader = ShaderDeclaration(vertShaderFile, fragShaderFile, defs, options, plugins),
        uniforms = uniforms
    )
}

class Effect<U : UniformSupplier> internal constructor(internal val fragFile: String, internal val uniformFactory: () -> U)

class Sky internal constructor(internal val pluginShaderFile: String)

object MaterialModifiers {
    fun vertex(vertShaderFile: String): MaterialModifier = MaterialModifier { it.vertShaderFile = vertShaderFile }
    fun fragment(fragShaderFile: String): MaterialModifier = MaterialModifier { it.fragShaderFile = fragShaderFile }
    fun defs(vararg defs: String): MaterialModifier = MaterialModifier { it.defs += setOf(*defs) }
    fun plugin(name: String, shaderFile: String): MaterialModifier = MaterialModifier { it.plugins[name] = shaderFile }
    fun uniforms(uniforms: UniformSupplier): MaterialModifier = MaterialModifier { it.uniforms = uniforms }

    fun options(vararg options: StandartMaterialOption): MaterialModifier = MaterialModifier { it.options += setOf(*options) }
    fun standart(vararg options: StandartMaterialOption, block: StandartUniforms.() -> Unit): MaterialModifier = MaterialModifier {
        it.options += setOf(*options)
        it.uniforms = StandartUniforms().apply(block)
    }

    fun <U : UniformSupplier> effect(effect: Effect<U>, block: U.() -> Unit = {}): MaterialModifier = MaterialModifier {
        it.fragShaderFile = effect.fragFile
        it.uniforms = effect.uniformFactory().apply(block)
    }

    fun sky(skyPreset: Sky): MaterialModifier = MaterialModifier {
        it.plugins["sky"] = skyPreset.pluginShaderFile
    }
}
