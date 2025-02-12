package com.zakgof.korender.impl.material

import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.impl.engine.BaseMaterial
import com.zakgof.korender.impl.engine.MaterialDeclaration
import com.zakgof.korender.impl.engine.ShaderDeclaration

internal fun interface InternalMaterialModifier : MaterialModifier {
    fun applyTo(builder: MaterialBuilder)
}

internal class MaterialBuilder(base: BaseMaterial, deferredShading: Boolean) {

    var vertShaderFile: String = when (base) {
        BaseMaterial.Renderable -> "!shader/standart.vert"
        BaseMaterial.Billboard -> "!shader/billboard.vert"
        BaseMaterial.Screen, BaseMaterial.Shading, BaseMaterial.Composition -> "!shader/screen.vert"
        BaseMaterial.Sky -> "!shader/sky/sky.vert"
    }
    var fragShaderFile: String = when (base) {
        BaseMaterial.Renderable, BaseMaterial.Billboard -> if (deferredShading) "!shader/deferred/geometry.frag" else "!shader/forward.frag"
        BaseMaterial.Screen -> "!shader/screen.frag"
        BaseMaterial.Sky -> "!shader/sky/sky.frag"
        BaseMaterial.Shading -> "!shader/deferred/shading.frag"
        BaseMaterial.Composition -> "!shader/deferred/composition.frag"
    }

    val shaderDefs: MutableSet<String> = mutableSetOf()
    val plugins: MutableMap<String, String> = mutableMapOf()
    var uniforms: MutableMap<String, Any?> = mutableMapOf()

    fun toMaterialDeclaration(): MaterialDeclaration = MaterialDeclaration(
        shader = ShaderDeclaration(vertShaderFile, fragShaderFile, shaderDefs, plugins),
        uniforms = uniforms
    )
}

internal fun materialDeclaration(base: BaseMaterial, deferredShading: Boolean, vararg materialModifiers: MaterialModifier) =
    materialDeclarationBuilder(base, deferredShading, *materialModifiers).toMaterialDeclaration()

internal fun materialDeclarationBuilder(base: BaseMaterial, deferredShading: Boolean, vararg materialModifiers: MaterialModifier) =
    materialModifiers.fold(MaterialBuilder(base, deferredShading)) { acc, mod ->
        (mod as InternalMaterialModifier).applyTo(acc)
        acc
    }