package com.zakgof.korender.impl.engine

import com.zakgof.korender.FrameInfo
import com.zakgof.korender.camera.Camera
import com.zakgof.korender.context.GuiContainerContext
import com.zakgof.korender.context.InstancedBillboardsContext
import com.zakgof.korender.context.InstancedRenderablesContext
import com.zakgof.korender.context.PassContext
import com.zakgof.korender.material.MaterialBuilder
import com.zakgof.korender.material.MaterialModifier
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.mesh.Billboard
import com.zakgof.korender.mesh.InstancedBillboard
import com.zakgof.korender.mesh.InstancedMesh
import com.zakgof.korender.mesh.MeshDeclaration
import com.zakgof.korender.mesh.ScreenQuad
import com.zakgof.korender.projection.Projection

internal class DefaultPassContext(
    private val passDeclaration: PassDeclaration,
    override val frameInfo: FrameInfo,
    override val width: Int,
    override val height: Int,
    override var projection: Projection,
    override var camera: Camera,
    override var light: Vec3
) : PassContext {

    override fun Renderable(vararg materialModifiers: MaterialModifier, mesh: MeshDeclaration, transform: Transform, transparent: Boolean) {
        val materialDeclaration = materialDeclaration(MaterialBuilder(), materialModifiers)
        passDeclaration.add(RenderableDeclaration(mesh, materialDeclaration.shader, materialDeclaration.uniforms, transform, if (transparent) Bucket.TRANSPARENT else Bucket.OPAQUE))
    }

    override fun Billboard(vararg materialModifiers: MaterialModifier, position: Vec3, transparent: Boolean) {
        val materialDeclaration = materialDeclaration(MaterialBuilder(vertShaderFile = "shader/billboard.vert", fragShaderFile = "shader/standart.frag"), materialModifiers)
        passDeclaration.add(RenderableDeclaration(Billboard, materialDeclaration.shader, materialDeclaration.uniforms, translate(position), if (transparent) Bucket.TRANSPARENT else Bucket.OPAQUE))
    }

    override fun Screen(vararg materialModifiers: MaterialModifier) {
        val materialDeclaration = materialDeclaration(MaterialBuilder(vertShaderFile = "shader/screen.vert", fragShaderFile = "shader/screen.frag"), materialModifiers)
        passDeclaration.add(RenderableDeclaration(ScreenQuad, materialDeclaration.shader, materialDeclaration.uniforms, Transform(), Bucket.SCREEN))
    }

    override fun Sky(vararg materialModifiers: MaterialModifier) {
        val materialDeclaration = materialDeclaration(MaterialBuilder(vertShaderFile = "shader/sky/sky.vert", fragShaderFile = "shader/sky/sky.frag"), materialModifiers)
        passDeclaration.add(RenderableDeclaration(ScreenQuad, materialDeclaration.shader, materialDeclaration.uniforms, Transform(), Bucket.SKY))
    }

    override fun Gui(block: GuiContainerContext.() -> Unit) {
        val root = ElementDeclaration.Container(Direction.Vertical)
        DefaultContainerContext(root).apply(block)
        passDeclaration.addGui(root)
    }

    override fun InstancedRenderables(vararg materialModifiers: MaterialModifier, id: Any, count: Int, mesh: MeshDeclaration, static: Boolean, transparent: Boolean, block: InstancedRenderablesContext.() -> Unit) {
        val materialDeclaration = materialDeclaration(MaterialBuilder(), materialModifiers)
        passDeclaration.add(
            RenderableDeclaration(
                InstancedMesh(id, count, mesh, materialDeclaration, static, transparent, block),
                materialDeclaration.shader,
                materialDeclaration.uniforms
            )
        )
    }

    override fun InstancedBillboards(vararg materialModifiers: MaterialModifier, id: Any, count: Int, transparent: Boolean, block: InstancedBillboardsContext.() -> Unit) {
        val materialDeclaration = materialDeclaration(MaterialBuilder(vertShaderFile = "shader/billboard.vert", fragShaderFile = "shader/standart.frag"), materialModifiers)
        passDeclaration.add(
            RenderableDeclaration(
                InstancedBillboard(id, count, transparent, block),
                materialDeclaration.shader,
                materialDeclaration.uniforms
            )
        )
    }

    private fun materialDeclaration(builder: MaterialBuilder, materialModifiers: Array<out MaterialModifier>) =
        materialModifiers.fold(builder) { acc: MaterialBuilder, mod: MaterialModifier ->
            mod.applyTo(acc)
            acc
        }.toMaterialDeclaration()


    override fun Camera(camera: Camera) {
        this.camera = camera
    }

    override fun Projection(projection: Projection) {
        this.projection = projection
    }

    override fun Light(light: Vec3) {
        this.light = light
    }

}