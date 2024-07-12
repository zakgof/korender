package com.zakgof.korender.impl.engine

import com.zakgof.korender.FrameInfo
import com.zakgof.korender.camera.Camera
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.context.GuiContainerContext
import com.zakgof.korender.context.InstancedBillboardsContext
import com.zakgof.korender.context.InstancedRenderablesContext
import com.zakgof.korender.context.PassContext
import com.zakgof.korender.context.ShadowContext
import com.zakgof.korender.material.MaterialModifier
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.mesh.MeshDeclaration
import com.zakgof.korender.projection.Projection

internal class DefaultFrameContext(
    private val sceneDeclaration: SceneDeclaration,
    override val frameInfo: FrameInfo,
    override val width: Int,
    override val height: Int,
    override var projection: Projection,
    override var camera: Camera,
    override var light: Vec3
) : FrameContext {

    private val defaultPassContext = DefaultPassContext(sceneDeclaration.defaultPass, frameInfo, width, height, projection, camera, light)

    override fun Pass(block: PassContext.() -> Unit) {
        val passDeclaration = PassDeclaration()
        DefaultPassContext(passDeclaration, frameInfo, width, height, projection, camera, light).apply(block)
        sceneDeclaration.addPass(passDeclaration)
    }

    override fun Renderable(vararg materialModifiers: MaterialModifier, mesh: MeshDeclaration, transform: Transform, transparent: Boolean) =
        defaultPassContext.Renderable(*materialModifiers, mesh = mesh, transform = transform, transparent = transparent)

    override fun Billboard(vararg materialModifiers: MaterialModifier, position: Vec3, transparent: Boolean) =
        defaultPassContext.Billboard(*materialModifiers, position = position, transparent = transparent)

    override fun Screen(vararg materialModifiers: MaterialModifier) =
        defaultPassContext.Screen(*materialModifiers)

    override fun Sky(vararg materialModifiers: MaterialModifier) =
        defaultPassContext.Sky(*materialModifiers)

    override fun Gui(block: GuiContainerContext.() -> Unit) =
        defaultPassContext.Gui(block)

    override fun InstancedRenderables(vararg materialModifiers: MaterialModifier, id: Any, count: Int, mesh: MeshDeclaration, static: Boolean, transparent: Boolean, block: InstancedRenderablesContext.() -> Unit) =
        defaultPassContext.InstancedRenderables(*materialModifiers, id = id, count = count, mesh = mesh, static = static, transparent = transparent, block = block)

    override fun InstancedBillboards(vararg materialModifiers: MaterialModifier, id: Any, count: Int, transparent: Boolean, block: InstancedBillboardsContext.() -> Unit) =
        defaultPassContext.InstancedBillboards(*materialModifiers, id = id, count = count, transparent = transparent, block = block)

    override fun Shadow(block: ShadowContext.() -> Unit) {
        val shadowDeclaration = ShadowDeclaration()
        ShadowContext(shadowDeclaration).apply(block)
        sceneDeclaration.addShadow(shadowDeclaration)
    }

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