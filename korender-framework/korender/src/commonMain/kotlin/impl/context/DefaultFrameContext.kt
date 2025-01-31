package com.zakgof.korender.impl.context

import com.zakgof.korender.FrameInfo
import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.context.GuiContainerContext
import com.zakgof.korender.context.InstancedBillboardsContext
import com.zakgof.korender.context.InstancedRenderablesContext
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.context.ShadowContext
import com.zakgof.korender.impl.engine.BaseMaterial
import com.zakgof.korender.impl.engine.Bucket
import com.zakgof.korender.impl.engine.DirectionalLightDeclaration
import com.zakgof.korender.impl.engine.ElementDeclaration
import com.zakgof.korender.impl.engine.GltfDeclaration
import com.zakgof.korender.impl.engine.PointLightDeclaration
import com.zakgof.korender.impl.engine.RenderableDeclaration
import com.zakgof.korender.impl.engine.SceneDeclaration
import com.zakgof.korender.impl.engine.ShadowDeclaration
import com.zakgof.korender.impl.geometry.InstancedBillboard
import com.zakgof.korender.impl.geometry.InstancedMesh
import com.zakgof.korender.impl.geometry.ScreenQuad
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3

internal class DefaultFrameContext(
    private val korenderContext: KorenderContext,
    private val sceneDeclaration: SceneDeclaration,
    override val frameInfo: FrameInfo,
) : FrameContext, KorenderContext by korenderContext {

    override fun DeferredShading(vararg compositionModifiers: MaterialModifier) {
        sceneDeclaration.deferredShading = true
        sceneDeclaration.compositionModifiers += compositionModifiers
    }

    override fun Scene(gltfResource: String, transform: Transform, time: Float?) {
        sceneDeclaration.gltfs += GltfDeclaration(gltfResource, transform, time ?: frameInfo.time)
    }

    override fun Renderable(vararg materialModifiers: MaterialModifier, mesh: MeshDeclaration, transform: Transform, transparent: Boolean) {
        sceneDeclaration.renderables += RenderableDeclaration(BaseMaterial.Renderable, materialModifiers.asList(), mesh,  transform, if (transparent) Bucket.TRANSPARENT else Bucket.OPAQUE)
    }

    override fun Billboard(vararg materialModifiers: MaterialModifier, position: Vec3, transparent: Boolean) {
        sceneDeclaration.renderables += RenderableDeclaration(BaseMaterial.Billboard, materialModifiers.asList(), com.zakgof.korender.impl.geometry.Billboard, translate(position), if (transparent) Bucket.TRANSPARENT else Bucket.OPAQUE)
    }

    override fun Screen(vararg materialModifiers: MaterialModifier) {
        sceneDeclaration.renderables += RenderableDeclaration(BaseMaterial.Screen, materialModifiers.asList(),  ScreenQuad, Transform(), Bucket.SCREEN)
    }

    override fun Sky(vararg materialModifiers: MaterialModifier) {
        sceneDeclaration.renderables += RenderableDeclaration(BaseMaterial.Sky, materialModifiers.asList(), ScreenQuad, Transform(), Bucket.SKY)
    }

    override fun Gui(block: GuiContainerContext.() -> Unit) {
        val root = ElementDeclaration.Container(Direction.Vertical)
        DefaultContainerContext(this, root).apply(block)
        sceneDeclaration.guis += root
    }

    override fun InstancedRenderables(vararg materialModifiers: MaterialModifier, id: Any, count: Int, mesh: MeshDeclaration, static: Boolean, transparent: Boolean, block: InstancedRenderablesContext.() -> Unit) {
        sceneDeclaration.renderables +=
            RenderableDeclaration(
                BaseMaterial.Renderable, materialModifiers.asList(),
                InstancedMesh(id, count, mesh, static, transparent, block),
                transform = Transform(),
                if (transparent) Bucket.TRANSPARENT else Bucket.OPAQUE
            )
    }

    override fun InstancedBillboards(vararg materialModifiers: MaterialModifier, id: Any, count: Int, transparent: Boolean, block: InstancedBillboardsContext.() -> Unit) {
        sceneDeclaration.renderables +=
            RenderableDeclaration(
                BaseMaterial.Billboard, materialModifiers.asList(),
                InstancedBillboard(id, count, transparent, block),
                transform = Transform(),
                if (transparent) Bucket.TRANSPARENT else Bucket.OPAQUE
            )
    }

    override fun DirectionalLight(direction: Vec3, color: Color, block: ShadowContext.() -> Unit) {
        val shadowDeclaration = ShadowDeclaration()
        DefaultShadowContext(shadowDeclaration).apply(block)
        sceneDeclaration.directionalLights += DirectionalLightDeclaration(direction, color, shadowDeclaration)
    }

    override fun PointLight(position: Vec3, color: Color) {
        sceneDeclaration.pointLights += PointLightDeclaration(position, color)
    }

    override fun AmbientLight(color: Color) {
        sceneDeclaration.ambientLightColor = color
    }

    override fun Filter(vararg materialModifiers: MaterialModifier) {
        sceneDeclaration.filters += materialModifiers.asList()
    }
}