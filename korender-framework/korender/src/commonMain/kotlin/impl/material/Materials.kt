package com.zakgof.korender.impl.material

import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.StandartMaterialOption
import com.zakgof.korender.impl.engine.MaterialDeclaration
import com.zakgof.korender.impl.engine.ShaderDeclaration

internal fun interface InternalMaterialModifier : MaterialModifier {
    fun applyTo(builder: MaterialBuilder)
}

internal class MaterialBuilder(
    var vertShaderFile: String = "!shader/standart.vert",
    var fragShaderFile: String = "!shader/standart.frag",
    val options: MutableSet<StandartMaterialOption> = mutableSetOf(),
    val defs: MutableSet<String> = mutableSetOf(),
    val plugins: MutableMap<String, String> = mutableMapOf(),
    var shaderUniforms: DynamicUniforms = BaseParamUniforms(InternalStandartParams()) {},
    val pluginUniforms: MutableList<DynamicUniforms> = mutableListOf()
) {
    fun toMaterialDeclaration(): MaterialDeclaration = MaterialDeclaration(
        shader = ShaderDeclaration(vertShaderFile, fragShaderFile, defs, options, plugins),
        uniforms = { pluginUniforms.fold(shaderUniforms()) { acc, pu -> acc + pu() } }
    )
}