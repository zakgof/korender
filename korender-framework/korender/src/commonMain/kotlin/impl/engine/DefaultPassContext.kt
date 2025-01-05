package com.zakgof.korender.impl.engine

import com.zakgof.korender.FrameInfo
import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.context.GuiContainerContext
import com.zakgof.korender.context.InstancedBillboardsContext
import com.zakgof.korender.context.InstancedRenderablesContext
import com.zakgof.korender.context.PassContext
import com.zakgof.korender.impl.geometry.Billboard
import com.zakgof.korender.impl.geometry.InstancedBillboard
import com.zakgof.korender.impl.geometry.InstancedMesh
import com.zakgof.korender.impl.geometry.ScreenQuad
import com.zakgof.korender.impl.material.InternalMaterialModifier
import com.zakgof.korender.impl.material.MaterialBuilder
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3

internal class DefaultPassContext(
    private val passDeclaration: PassDeclaration,
    override val frameInfo: FrameInfo
) : PassContext {

    override fun Scene(gltfResource: String, transform: Transform) {
        passDeclaration.gltfs += GltfDeclaration(gltfResource, transform)
    }

    override fun Renderable(vararg materialModifiers: MaterialModifier, mesh: MeshDeclaration, transform: Transform, transparent: Boolean) {
        val materialDeclaration = materialDeclaration(MaterialBuilder(), materialModifiers)
        passDeclaration.renderables += RenderableDeclaration(mesh, materialDeclaration.shader, materialDeclaration.uniforms, transform, if (transparent) Bucket.TRANSPARENT else Bucket.OPAQUE)
    }

    override fun Billboard(vararg materialModifiers: MaterialModifier, position: Vec3, transparent: Boolean) {
        val materialDeclaration = materialDeclaration(MaterialBuilder(vertShaderFile = "!shader/billboard.vert", fragShaderFile = "!shader/standart.frag"), materialModifiers)
        passDeclaration.renderables += RenderableDeclaration(Billboard, materialDeclaration.shader, materialDeclaration.uniforms, translate(position), if (transparent) Bucket.TRANSPARENT else Bucket.OPAQUE)
    }

    override fun Screen(vararg materialModifiers: MaterialModifier) {
        val materialDeclaration = materialDeclaration(MaterialBuilder(vertShaderFile = "!shader/screen.vert", fragShaderFile = "!shader/screen.frag"), materialModifiers)
        passDeclaration.renderables += RenderableDeclaration(ScreenQuad, materialDeclaration.shader, materialDeclaration.uniforms, Transform(), Bucket.SCREEN)
    }

    override fun Sky(vararg materialModifiers: MaterialModifier) {
        val materialDeclaration = materialDeclaration(MaterialBuilder(vertShaderFile = "!shader/sky/sky.vert", fragShaderFile = "!shader/sky/sky.frag"), materialModifiers)
        passDeclaration.renderables += RenderableDeclaration(ScreenQuad, materialDeclaration.shader, materialDeclaration.uniforms, Transform(), Bucket.SKY)
    }

    override fun Gui(block: GuiContainerContext.() -> Unit) {
        val root = ElementDeclaration.Container(Direction.Vertical)
        DefaultContainerContext(root).apply(block)
        passDeclaration.guis += root
    }

    override fun InstancedRenderables(vararg materialModifiers: MaterialModifier, id: Any, count: Int, mesh: MeshDeclaration, static: Boolean, transparent: Boolean, block: InstancedRenderablesContext.() -> Unit) {
        val materialDeclaration = materialDeclaration(MaterialBuilder(), materialModifiers)
        passDeclaration.renderables +=
            RenderableDeclaration(
                InstancedMesh(id, count, mesh, materialDeclaration, static, transparent, block),
                materialDeclaration.shader,
                materialDeclaration.uniforms
            )
    }

    override fun InstancedBillboards(vararg materialModifiers: MaterialModifier, id: Any, count: Int, transparent: Boolean, block: InstancedBillboardsContext.() -> Unit) {
        val materialDeclaration = materialDeclaration(MaterialBuilder(vertShaderFile = "!shader/billboard.vert", fragShaderFile = "!shader/standart.frag"), materialModifiers)
        passDeclaration.renderables +=
            RenderableDeclaration(
                InstancedBillboard(id, count, transparent, block),
                materialDeclaration.shader,
                materialDeclaration.uniforms
            )

    }

    override fun DirectionalLight(direction: Vec3, color: Color) {
        passDeclaration.directionalLights += DirectionalLightDeclaration(direction, color)
    }

    override fun PointLight(position: Vec3, color: Color) {
        passDeclaration.pointLights += PointLightDeclaration(position, color)
    }

    override fun AmbientLight(color: Color) {
        passDeclaration.ambientLightColor = color
    }

    private fun materialDeclaration(builder: MaterialBuilder, materialModifiers: Array<out MaterialModifier>) =
        materialModifiers.fold(builder) { acc, mod ->
            (mod as InternalMaterialModifier).applyTo(acc)
            acc
        }.toMaterialDeclaration()
}