package com.zakgof.korender.impl.context

import com.zakgof.korender.FrameInfo
import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.Prefab
import com.zakgof.korender.context.DeferredShadingContext
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.context.GuiContainerContext
import com.zakgof.korender.context.InstancedBillboardsContext
import com.zakgof.korender.context.InstancedRenderablesContext
import com.zakgof.korender.context.InstancingDeclaration
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.context.ShadowContext
import com.zakgof.korender.impl.engine.BaseMaterial
import com.zakgof.korender.impl.engine.Bucket
import com.zakgof.korender.impl.engine.CaptureContext
import com.zakgof.korender.impl.engine.DeferredShadingDeclaration
import com.zakgof.korender.impl.engine.DirectionalLightDeclaration
import com.zakgof.korender.impl.engine.ElementDeclaration
import com.zakgof.korender.impl.engine.GltfDeclaration
import com.zakgof.korender.impl.engine.InternalInstancingDeclaration
import com.zakgof.korender.impl.engine.PointLightDeclaration
import com.zakgof.korender.impl.engine.RenderableDeclaration
import com.zakgof.korender.impl.engine.SceneDeclaration
import com.zakgof.korender.impl.engine.ShadowDeclaration
import com.zakgof.korender.impl.geometry.InstancedBillboard
import com.zakgof.korender.impl.geometry.InstancedMesh
import com.zakgof.korender.impl.geometry.ScreenQuad
import com.zakgof.korender.impl.prefab.InternalPrefab
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3

internal class DefaultFrameContext(
    private val korenderContext: KorenderContext,
    private val sceneDeclaration: SceneDeclaration,
    override val frameInfo: FrameInfo,
) : FrameContext, KorenderContext by korenderContext {

    override fun DeferredShading(block: DeferredShadingContext.() -> Unit) {
        sceneDeclaration.deferredShadingDeclaration = DeferredShadingDeclaration()
        DefaultDeferredShadingContext(sceneDeclaration.deferredShadingDeclaration!!).apply(block)
    }

    override fun Gltf(resource: String, animation: Int, transform: Transform, time: Float?) {
        sceneDeclaration.gltfs += GltfDeclaration(resource, animation, transform, time ?: frameInfo.time)
    }

    override fun Renderable(vararg materialModifiers: MaterialModifier, mesh: MeshDeclaration, transform: Transform, transparent: Boolean, instancing: InstancingDeclaration?) {
        val meshDeclaration = (instancing as? InternalInstancingDeclaration)?.let { InstancedMesh(instancing.id, instancing.instanceCount, mesh, !instancing.dynamic, transparent, instancing.block) } ?: mesh;
        sceneDeclaration.renderables += RenderableDeclaration(BaseMaterial.Renderable, materialModifiers.asList(), meshDeclaration, transform, if (transparent) Bucket.TRANSPARENT else Bucket.OPAQUE)
    }

    override fun Renderable(vararg materialModifiers: MaterialModifier, prefab: Prefab) {
        (prefab as InternalPrefab).render(this, *materialModifiers)
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

    override fun DirectionalLight(direction: Vec3, color: ColorRGB, block: ShadowContext.() -> Unit) {
        val shadowDeclaration = ShadowDeclaration()
        DefaultShadowContext(shadowDeclaration).apply(block)
        sceneDeclaration.directionalLights += DirectionalLightDeclaration(direction.normalize(), color, shadowDeclaration)
    }

    override fun PointLight(position: Vec3, color: ColorRGB, attenuationLinear: Float, attenuationQuadratic: Float) {
        sceneDeclaration.pointLights += PointLightDeclaration(position, color, Vec3(attenuationLinear, attenuationQuadratic, 0f))
    }

    override fun AmbientLight(color: ColorRGB) {
        sceneDeclaration.ambientLightColor = color
    }

    override fun PostProcess(vararg materialModifiers: MaterialModifier) {
        sceneDeclaration.filters += materialModifiers.asList()
    }

    override fun CaptureEnv(slot: Int, resolution: Int, position: Vec3, near: Float, far: Float, block: FrameContext.() -> Unit) {
        val captureSceneDeclaration = SceneDeclaration()
        val captureContext = CaptureContext(resolution, position, near, far, captureSceneDeclaration)
        DefaultFrameContext(korenderContext, captureSceneDeclaration, frameInfo).apply(block)
        sceneDeclaration.captures[slot] = captureContext
    }
}