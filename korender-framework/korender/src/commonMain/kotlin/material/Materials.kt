package com.zakgof.korender.material

import com.zakgof.korender.impl.engine.MaterialDeclaration
import com.zakgof.korender.impl.engine.ShaderDeclaration
import com.zakgof.korender.uniforms.StandartUniforms
import com.zakgof.korender.uniforms.UniformSupplier

fun interface MaterialModifier {
    fun applyTo(builder: MaterialBuilder)
}

class MaterialBuilder internal constructor(
    var vertShaderFile: String = "shader/standart.vert",
    var fragShaderFile: String = "shader/standart.frag",
    val options: MutableSet<StandartMaterialOption> = mutableSetOf(),
    val defs: MutableSet<String> = mutableSetOf(),
    val plugins: MutableMap<String, String> = mutableMapOf(),
    var uniforms: Set<UniformSupplier> = setOf()
) {
    internal fun toMaterialDeclaration(): MaterialDeclaration = MaterialDeclaration(
        shader = ShaderDeclaration(vertShaderFile, fragShaderFile, defs, options, plugins),
        uniforms = {key -> uniforms.map{it[key]}.filterNotNull().firstOrNull()}
    )
}

class Effect<U : UniformSupplier> internal constructor(internal val fragFile: String, internal val uniformFactory: () -> U)

class Sky<U : UniformSupplier> internal constructor(internal val pluginShaderFile: String, internal val uniformFactory: () -> U)

object MaterialModifiers {
    fun vertex(vertShaderFile: String): MaterialModifier = MaterialModifier { it.vertShaderFile = vertShaderFile }
    fun fragment(fragShaderFile: String): MaterialModifier = MaterialModifier { it.fragShaderFile = fragShaderFile }
    fun defs(vararg defs: String): MaterialModifier = MaterialModifier { it.defs += setOf(*defs) }
    fun plugin(name: String, shaderFile: String): MaterialModifier = MaterialModifier { it.plugins[name] = shaderFile }
    fun uniforms(uniforms: UniformSupplier): MaterialModifier = MaterialModifier { it.uniforms += uniforms }

    fun options(vararg options: StandartMaterialOption): MaterialModifier = MaterialModifier { it.options += setOf(*options) }
    fun standart(vararg options: StandartMaterialOption, block: StandartUniforms.() -> Unit): MaterialModifier = MaterialModifier {
        it.options += setOf(*options)
        it.uniforms += StandartUniforms().apply(block)
    }

    fun <U : UniformSupplier> effect(effect: Effect<U>, block: U.() -> Unit = {}): MaterialModifier = MaterialModifier {
        it.fragShaderFile = effect.fragFile
        it.uniforms += effect.uniformFactory().apply(block)
    }

    fun <U : UniformSupplier> sky(sky: Sky<U>, block: U.() -> Unit = {}): MaterialModifier = MaterialModifier {
        it.plugins["sky"] = sky.pluginShaderFile
        it.uniforms += sky.uniformFactory().apply(block)
    }
}
