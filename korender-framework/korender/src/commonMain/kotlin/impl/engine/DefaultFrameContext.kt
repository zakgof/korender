package com.zakgof.korender.impl.engine

import com.zakgof.korender.FrameInfo
import com.zakgof.korender.camera.Camera
import com.zakgof.korender.declaration.BillboardMaterialDeclaration
import com.zakgof.korender.declaration.FilterDeclaration
import com.zakgof.korender.declaration.FrameContext
import com.zakgof.korender.declaration.GuiContainerContext
import com.zakgof.korender.declaration.InstancedBillboardsContext
import com.zakgof.korender.declaration.InstancedRenderablesContext
import com.zakgof.korender.declaration.MaterialDeclaration
import com.zakgof.korender.declaration.MeshDeclaration
import com.zakgof.korender.declaration.ShadowContext
import com.zakgof.korender.declaration.UniformSupplier
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.projection.Projection

internal class DefaultFrameContext(
    override val frameInfo: FrameInfo,
    private val sceneDeclaration: SceneDeclaration,
    override val width: Int,
    override val height: Int,
    override var projection: Projection,
    override var camera: Camera,
    override var light: Vec3
) : FrameContext {
    override fun Renderable(mesh: MeshDeclaration, material: MaterialDeclaration, transform: Transform, transparent: Boolean) {
        sceneDeclaration.add(RenderableDeclaration(mesh, material.shader, material.uniforms, transform, if (transparent) Bucket.TRANSPARENT else Bucket.OPAQUE))
    }

    override fun Billboard(material: BillboardMaterialDeclaration, position: Vec3, transparent: Boolean) {
        sceneDeclaration.add(
            RenderableDeclaration(
                mesh = MeshDeclaration.Billboard,
                shader = material.shader,
                uniforms = material.uniforms,
                transform = Transform().translate(position),
                bucket = if (transparent) Bucket.TRANSPARENT else Bucket.OPAQUE
            )
        )
    }

    override fun Filter(fragFile: String, vararg defs: String, plugins: Map<String, String>, uniforms: UniformSupplier) {
        sceneDeclaration.add(
            FilterDeclaration(
                ShaderDeclaration("screen.vert", fragFile, setOf(*defs), plugins),
                uniforms
            )
        )
    }

    override fun InstancedBillboards(id: Any, count: Int, material: BillboardMaterialDeclaration, zSort: Boolean, block: InstancedBillboardsContext.() -> Unit) {
        sceneDeclaration.add(
            RenderableDeclaration(
                MeshDeclaration.InstancedBillboard(id, count, zSort, block),
                material.shader,
                material.uniforms
            )
        )
    } // TODO Bucket

    override fun InstancedRenderables(id: Any, count: Int, mesh: MeshDeclaration, material: MaterialDeclaration, static: Boolean, block: InstancedRenderablesContext.() -> Unit) {
        sceneDeclaration.add(
            RenderableDeclaration(
                MeshDeclaration.InstancedMesh(id, count, mesh, material, static, block), material.shader, material.uniforms
            )
        )
    } // TODO Bucket

    override fun Gui(block: GuiContainerContext.() -> Unit) {
        val gui = ElementDeclaration.Container(Direction.Vertical)
        sceneDeclaration.gui = gui
        DefaultContainerContext(gui).apply(block)
    }

    override fun Sky(vararg defs: String, plugins: Map<String, String>, uniforms: UniformSupplier) {
        sceneDeclaration.add(
            RenderableDeclaration(
                MeshDeclaration.ScreenQuad,
                ShaderDeclaration("sky/sky.vert", "sky/sky.frag", setOf(*defs), plugins),
                uniforms
            )
        )
    }

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