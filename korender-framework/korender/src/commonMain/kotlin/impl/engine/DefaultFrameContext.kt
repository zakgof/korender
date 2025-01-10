package com.zakgof.korender.impl.engine

import com.zakgof.korender.FrameInfo
import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.context.GuiContainerContext
import com.zakgof.korender.context.InstancedBillboardsContext
import com.zakgof.korender.context.InstancedRenderablesContext
import com.zakgof.korender.context.ShadowContext
import com.zakgof.korender.impl.geometry.InstancedBillboard
import com.zakgof.korender.impl.geometry.InstancedMesh
import com.zakgof.korender.impl.geometry.ScreenQuad
import com.zakgof.korender.impl.material.MaterialBuilder
import com.zakgof.korender.impl.material.materialDeclaration
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3

internal class DefaultFrameContext(
    private val sceneDeclaration: SceneDeclaration,
    override val frameInfo: FrameInfo,
) : FrameContext {

    override fun Shadow(block: ShadowContext.() -> Unit) {
        val shadowDeclaration = ShadowDeclaration()
        ShadowContext(shadowDeclaration).apply(block)
        sceneDeclaration.addShadow(shadowDeclaration)
    }

    override fun Scene(gltfResource: String, transform: Transform) {
        sceneDeclaration.gltfs += GltfDeclaration(gltfResource, transform)
    }

    override fun Renderable(vararg materialModifiers: MaterialModifier, mesh: MeshDeclaration, transform: Transform, transparent: Boolean) {
        val materialDeclaration = materialDeclaration(MaterialBuilder(), *materialModifiers)
        sceneDeclaration.renderables += RenderableDeclaration(mesh, materialDeclaration.shader, materialDeclaration.uniforms, transform, if (transparent) Bucket.TRANSPARENT else Bucket.OPAQUE)
    }

    override fun Billboard(vararg materialModifiers: MaterialModifier, position: Vec3, transparent: Boolean) {
        val materialDeclaration = materialDeclaration(MaterialBuilder(vertShaderFile = "!shader/billboard.vert", fragShaderFile = "!shader/standart.frag"), *materialModifiers)
        sceneDeclaration.renderables += RenderableDeclaration(com.zakgof.korender.impl.geometry.Billboard, materialDeclaration.shader, materialDeclaration.uniforms, translate(position), if (transparent) Bucket.TRANSPARENT else Bucket.OPAQUE)
    }

    override fun Screen(vararg materialModifiers: MaterialModifier) {
        val materialDeclaration = materialDeclaration(MaterialBuilder(vertShaderFile = "!shader/screen.vert", fragShaderFile = "!shader/screen.frag"), *materialModifiers)
        sceneDeclaration.renderables += RenderableDeclaration(ScreenQuad, materialDeclaration.shader, materialDeclaration.uniforms, Transform(), Bucket.SCREEN)
    }

    override fun Sky(vararg materialModifiers: MaterialModifier) {
        val materialDeclaration = materialDeclaration(MaterialBuilder(vertShaderFile = "!shader/sky/sky.vert", fragShaderFile = "!shader/sky/sky.frag"), *materialModifiers)
        sceneDeclaration.renderables += RenderableDeclaration(ScreenQuad, materialDeclaration.shader, materialDeclaration.uniforms, Transform(), Bucket.SKY)
    }

    override fun Gui(block: GuiContainerContext.() -> Unit) {
        val root = ElementDeclaration.Container(Direction.Vertical)
        DefaultContainerContext(root).apply(block)
        sceneDeclaration.guis += root
    }

    override fun InstancedRenderables(vararg materialModifiers: MaterialModifier, id: Any, count: Int, mesh: MeshDeclaration, static: Boolean, transparent: Boolean, block: InstancedRenderablesContext.() -> Unit) {
        val materialDeclaration = materialDeclaration(MaterialBuilder(), *materialModifiers)
        sceneDeclaration.renderables +=
            RenderableDeclaration(
                InstancedMesh(id, count, mesh, materialDeclaration, static, transparent, block),
                materialDeclaration.shader,
                materialDeclaration.uniforms
            )
    }

    override fun InstancedBillboards(vararg materialModifiers: MaterialModifier, id: Any, count: Int, transparent: Boolean, block: InstancedBillboardsContext.() -> Unit) {
        val materialDeclaration = materialDeclaration(MaterialBuilder(vertShaderFile = "!shader/billboard.vert", fragShaderFile = "!shader/standart.frag"), *materialModifiers)
        sceneDeclaration.renderables +=
            RenderableDeclaration(
                InstancedBillboard(id, count, transparent, block),
                materialDeclaration.shader,
                materialDeclaration.uniforms
            )

    }

    override fun DirectionalLight(direction: Vec3, color: Color) {
        sceneDeclaration.directionalLights += DirectionalLightDeclaration(direction, color)
    }

    override fun PointLight(position: Vec3, color: Color) {
        sceneDeclaration.pointLights += PointLightDeclaration(position, color)
    }

    override fun AmbientLight(color: Color) {
        sceneDeclaration.ambientLightColor = color
    }

    override fun Filter(vararg materialModifiers: MaterialModifier) {
        sceneDeclaration.filters += materialDeclaration(
            MaterialBuilder(vertShaderFile = "!shader/screen.vert", fragShaderFile = "!shader/screen.frag"),
            *materialModifiers
        )
    }
}