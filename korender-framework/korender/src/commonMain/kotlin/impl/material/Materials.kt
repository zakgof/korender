package com.zakgof.korender.impl.material

import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.RenderingOption
import com.zakgof.korender.impl.engine.BaseMaterial
import com.zakgof.korender.impl.engine.MaterialDeclaration
import com.zakgof.korender.impl.engine.ShaderDeclaration

internal fun interface InternalMaterialModifier : MaterialModifier {
    fun applyTo(builder: MaterialBuilder)
}

internal class MaterialBuilder(base: BaseMaterial, deferredShading: Boolean, capture: Boolean) {

    var vertShaderFile: String = when (base) {
        BaseMaterial.Renderable -> if (capture) "!shader/capture.vert" else "!shader/standart.vert"
        BaseMaterial.Billboard -> "!shader/billboard.vert"
        BaseMaterial.Screen, BaseMaterial.Composition -> "!shader/screen.vert"
        BaseMaterial.Sky -> "!shader/sky/sky.vert"
    }
    var fragShaderFile: String = when (base) {
        BaseMaterial.Renderable, BaseMaterial.Billboard -> if (deferredShading) "!shader/geometry.frag" else "!shader/forward.frag"
        BaseMaterial.Screen -> "!shader/screen.frag"
        BaseMaterial.Sky -> "!shader/sky/sky.frag"
        BaseMaterial.Composition -> "!shader/composition.frag"
    }

    val options: MutableSet<RenderingOption> = mutableSetOf()
    val shaderDefs: MutableSet<String> = mutableSetOf()
    val plugins: MutableMap<String, String> = mutableMapOf()
    var shaderUniforms: DynamicUniforms = ParamUniforms(InternalStandartParams()) {}
    val pluginUniforms: MutableList<DynamicUniforms> = mutableListOf()

    fun toMaterialDeclaration(): MaterialDeclaration = MaterialDeclaration(
        shader = ShaderDeclaration(vertShaderFile, fragShaderFile, shaderDefs, options, plugins),
        uniforms = { pluginUniforms.fold(shaderUniforms()) { acc, pu -> acc + pu() } }
    )
}

internal fun materialDeclaration(base: BaseMaterial, deferredShading: Boolean, capture: Boolean, vararg materialModifiers: MaterialModifier) =
    materialModifiers.fold(MaterialBuilder(base, deferredShading, capture)) { acc, mod ->
        (mod as InternalMaterialModifier).applyTo(acc)
        acc
    }.toMaterialDeclaration()