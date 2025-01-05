package com.zakgof.korender.impl.material

import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.RenderingOption
import com.zakgof.korender.impl.engine.MaterialDeclaration
import com.zakgof.korender.impl.engine.ShaderDeclaration

internal fun interface InternalMaterialModifier : MaterialModifier {
    fun applyTo(builder: MaterialBuilder)
}

internal class MaterialBuilder(
    var vertShaderFile: String = "!shader/standart.vert",
    var fragShaderFile: String = "!shader/standart.frag",
    val options: MutableSet<RenderingOption> = mutableSetOf(),
    val shaderDefs: MutableSet<String> = mutableSetOf(),
    val plugins: MutableMap<String, String> = mutableMapOf(),
    var shaderUniforms: DynamicUniforms = ParamUniforms(InternalStandartParams()) {},
    val pluginUniforms: MutableList<DynamicUniforms> = mutableListOf()
) {
    fun toMaterialDeclaration(): MaterialDeclaration = MaterialDeclaration(
        shader = ShaderDeclaration(vertShaderFile, fragShaderFile, shaderDefs, options, plugins),
        uniforms = { pluginUniforms.fold(shaderUniforms()) { acc, pu -> acc + pu() } }
    )
}