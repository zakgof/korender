package com.zakgof.korender.declaration

import com.zakgof.korender.FrameInfo
import com.zakgof.korender.SceneDeclaration
import com.zakgof.korender.camera.Camera
import com.zakgof.korender.impl.engine.Bucket
import com.zakgof.korender.impl.engine.CascadeDeclaration
import com.zakgof.korender.impl.engine.CustomShaderDeclaration
import com.zakgof.korender.impl.engine.ElementDeclaration
import com.zakgof.korender.impl.engine.FilterDeclaration
import com.zakgof.korender.impl.engine.RenderableDeclaration
import com.zakgof.korender.impl.engine.ShadowDeclaration
import com.zakgof.korender.impl.material.StockUniforms
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.projection.Projection

class SceneContext internal constructor(val frameInfo: FrameInfo, private val sceneDeclaration: SceneDeclaration, val width: Int, val height: Int, var projection: Projection, var camera: Camera, var light: Vec3) {
    fun Renderable(
        mesh: MeshDeclaration,
        material: MaterialDeclaration,
        transform: Transform = Transform(),
        transparent: Boolean = false
    ) = sceneDeclaration.add(
        RenderableDeclaration(mesh, material.shader, material.uniforms, transform, if (transparent) Bucket.TRANSPARENT else Bucket.OPAQUE)
    )

    fun Billboard(position: Vec3 = Vec3.ZERO, fragment: String = "standard.frag", vararg defs: String, material: StockUniforms.() -> Unit, transparent: Boolean = false) =
        sceneDeclaration.add(
            RenderableDeclaration(
                mesh = MeshDeclaration.Billboard,
                shader = CustomShaderDeclaration("billboard.vert", fragment, defs.toSet()),
                uniforms = StockUniforms().apply(material),
                transform = Transform().translate(position),
                bucket = if (transparent) Bucket.TRANSPARENT else Bucket.OPAQUE
            )
        )

    fun Filter(fragment: String, uniforms: UniformSupplier = UniformSupplier { null }) =
        sceneDeclaration.add(FilterDeclaration(fragment, uniforms))

    fun InstancedBillboards(
        id: Any,
        count: Int,
        zSort: Boolean = false,
        fragment: String = "standard.frag",
        material: StockUniforms.() -> Unit,
        block: InstancedBillboardsContext.() -> Unit
    ) =
        sceneDeclaration.add(
            RenderableDeclaration(
                MeshDeclaration.InstancedBillboard(id, count, zSort, block),
                CustomShaderDeclaration("billboard.vert", fragment),
                StockUniforms().apply(material)
            )
        ) // TODO Bucket

    fun InstancedRenderables(
        id: Any,
        count: Int,
        mesh: MeshDeclaration,
        material: MaterialDeclaration,
        static: Boolean = false,
        block: InstancedRenderablesContext.() -> Unit,
    ) =
        sceneDeclaration.add(
            RenderableDeclaration(
                MeshDeclaration.InstancedMesh(id, count, mesh, material, static, block),
                material.shader,
                material.uniforms
            )
        ) // TODO Bucket

    fun Gui(block: ContainerContext.() -> Unit) {
        val gui = ElementDeclaration.Container(Direction.Vertical)
        sceneDeclaration.gui = gui
        ContainerContext(gui).apply(block)
    }

    fun Sky(preset: String) {
        sceneDeclaration.add(
            RenderableDeclaration(
                MeshDeclaration.ScreenQuad,
                CustomShaderDeclaration("sky/sky.vert", "sky/${preset}sky.frag"),
                { null }
            )
        )
    }

    fun Shadow(block: ShadowContext.() -> Unit) {
        val shadowDeclaration = ShadowDeclaration()
        ShadowContext(shadowDeclaration).apply(block)
        sceneDeclaration.addShadow(shadowDeclaration)
    }

    fun Camera(camera: Camera) {
        this.camera = camera
    }

    fun Projection(projection: Projection) {
        this.projection = projection
    }

    fun Light(light: Vec3) {
        this.light = light
    }

}

class ShadowContext internal constructor(private val shadowDeclaration: ShadowDeclaration) {
    fun Cascade(mapSize: Int, near: Float, far: Float) =
        shadowDeclaration.addCascade(CascadeDeclaration(mapSize, near, far))
}