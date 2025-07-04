package com.zakgof.korender.impl.material

import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.impl.engine.BaseMaterial
import com.zakgof.korender.impl.engine.MaterialDeclaration
import com.zakgof.korender.impl.engine.ShaderDeclaration
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3.Companion.ZERO

internal fun interface InternalMaterialModifier : MaterialModifier {

    fun applyTo(builder: MaterialBuilder)

    operator fun plus(that: InternalMaterialModifier) = InternalMaterialModifier {
        this.applyTo(it)
        that.applyTo(it)
    }
}

internal class MaterialBuilder(base: BaseMaterial, deferredShading: Boolean, private val retentionPolicy: RetentionPolicy) {

    var vertShaderFile: String = when (base) {
        BaseMaterial.Renderable -> "!shader/base.vert"
        BaseMaterial.Billboard -> "!shader/billboard.vert"
        BaseMaterial.Screen, BaseMaterial.Shading, BaseMaterial.Composition -> "!shader/screen.vert"
        BaseMaterial.Font -> "!shader/gui/font.vert"
        BaseMaterial.Image -> "!shader/gui/image.vert"
        BaseMaterial.Sky -> "!shader/sky/sky.vert"
        BaseMaterial.Decal -> "!shader/deferred/decal.vert"
    }
    var fragShaderFile: String = when (base) {
        BaseMaterial.Renderable, BaseMaterial.Billboard -> if (deferredShading) "!shader/deferred/geometry.frag" else "!shader/forward.frag"
        BaseMaterial.Screen -> "!shader/screen.frag"
        BaseMaterial.Font -> "!shader/gui/font.frag"
        BaseMaterial.Image -> "!shader/gui/image.frag"
        BaseMaterial.Sky -> "!shader/sky/sky.frag"
        BaseMaterial.Shading -> "!shader/deferred/shading.frag"
        BaseMaterial.Composition -> "!shader/deferred/composition.frag"
        BaseMaterial.Decal -> "!shader/deferred/decal.frag"
    }

    val shaderDefs: MutableSet<String> = mutableSetOf()
    val plugins: MutableMap<String, String> = mutableMapOf()
    var uniforms: MutableMap<String, Any?> = mutableMapOf()

    init {
        if (base == BaseMaterial.Billboard) {
            uniforms["position"] = ZERO
            uniforms["scale"] = Vec2(1f, 1f)
            uniforms["rotation"] = 0.0f
        }
    }

    fun toMaterialDeclaration(): MaterialDeclaration = MaterialDeclaration(
        shader = ShaderDeclaration(vertShaderFile, fragShaderFile, shaderDefs, plugins, retentionPolicy),
        uniforms = uniforms
    )
}

internal fun materialDeclaration(base: BaseMaterial, deferredShading: Boolean, retentionPolicy: RetentionPolicy, materialModifiers: List<MaterialModifier>) =
    materialDeclarationBuilder(base, deferredShading, retentionPolicy, materialModifiers).toMaterialDeclaration()

internal fun materialDeclarationBuilder(base: BaseMaterial, deferredShading: Boolean, retentionPolicy: RetentionPolicy, materialModifiers: List<MaterialModifier>) =
    materialModifiers.fold(MaterialBuilder(base, deferredShading, retentionPolicy)) { acc, mod ->
        (mod as InternalMaterialModifier).applyTo(acc)
        acc
    }