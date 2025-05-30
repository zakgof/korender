package com.zakgof.korender.impl.context

import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.FrameInfo
import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.Prefab
import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.context.DeferredShadingContext
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.context.GuiContainerContext
import com.zakgof.korender.context.InstancedBillboardsContext
import com.zakgof.korender.context.InstancedRenderablesContext
import com.zakgof.korender.context.InstancingDeclaration
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.context.ShadowContext
import com.zakgof.korender.impl.camera.Camera
import com.zakgof.korender.impl.engine.BaseMaterial
import com.zakgof.korender.impl.engine.DeferredShadingDeclaration
import com.zakgof.korender.impl.engine.DirectionalLightDeclaration
import com.zakgof.korender.impl.engine.ElementDeclaration
import com.zakgof.korender.impl.engine.Engine
import com.zakgof.korender.impl.engine.EnvCaptureContext
import com.zakgof.korender.impl.engine.FrameCaptureContext
import com.zakgof.korender.impl.engine.GltfDeclaration
import com.zakgof.korender.impl.engine.InternalFilterDeclaration
import com.zakgof.korender.impl.engine.InternalInstancingDeclaration
import com.zakgof.korender.impl.engine.PointLightDeclaration
import com.zakgof.korender.impl.engine.RenderableDeclaration
import com.zakgof.korender.impl.engine.SceneDeclaration
import com.zakgof.korender.impl.engine.ShadowDeclaration
import com.zakgof.korender.impl.geometry.InstancedBillboard
import com.zakgof.korender.impl.geometry.InstancedMesh
import com.zakgof.korender.impl.geometry.ScreenQuad
import com.zakgof.korender.impl.prefab.InternalPrefab
import com.zakgof.korender.impl.projection.Projection
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3

internal class DefaultFrameContext(
    val korenderContext: Engine.KorenderContextImpl,
    private val sceneDeclaration: SceneDeclaration,
    override val frameInfo: FrameInfo,
) : FrameContext, KorenderContext by korenderContext {

    override fun DeferredShading(block: DeferredShadingContext.() -> Unit) {
        sceneDeclaration.deferredShadingDeclaration = DeferredShadingDeclaration()
        DefaultDeferredShadingContext(sceneDeclaration.deferredShadingDeclaration!!).apply(block)
    }

    override fun Gltf(resource: String, animation: Int, transform: Transform, time: Float?) {
        sceneDeclaration.gltfs += GltfDeclaration(resource, animation, transform, time ?: frameInfo.time, korenderContext.currentRetentionPolicy)
    }

    override fun Renderable(vararg materialModifiers: MaterialModifier, mesh: MeshDeclaration, transform: Transform, transparent: Boolean, instancing: InstancingDeclaration?) {
        val meshDeclaration = (instancing as? InternalInstancingDeclaration)?.let { InstancedMesh(instancing.id, instancing.instanceCount, mesh, !instancing.dynamic, transparent, korenderContext.currentRetentionPolicy, instancing.block) } ?: mesh;
        val rd = RenderableDeclaration(BaseMaterial.Renderable, materialModifiers.asList(), meshDeclaration, transform, korenderContext.currentRetentionPolicy)
        addToScene(transparent, rd)
    }

    override fun Renderable(vararg materialModifiers: MaterialModifier, prefab: Prefab) {
        (prefab as InternalPrefab).render(this, *materialModifiers)
    }

    override fun Billboard(vararg materialModifiers: MaterialModifier, position: Vec3, transparent: Boolean) {
        val rd = RenderableDeclaration(
            BaseMaterial.Billboard,
            materialModifiers.asList(),
            com.zakgof.korender.impl.geometry.Billboard(korenderContext.currentRetentionPolicy),
            translate(position),
            korenderContext.currentRetentionPolicy
        )
        addToScene(transparent, rd)
    }

    override fun Sky(vararg materialModifiers: MaterialModifier) {
        sceneDeclaration.skies += RenderableDeclaration(BaseMaterial.Sky, materialModifiers.asList(), ScreenQuad(korenderContext.currentRetentionPolicy), Transform(), korenderContext.currentRetentionPolicy)
    }

    override fun Gui(block: GuiContainerContext.() -> Unit) {
        val root = ElementDeclaration.Container(Direction.Vertical)
        DefaultContainerContext(this, root).apply(block)
        sceneDeclaration.guis += root
    }

    override fun InstancedRenderables(vararg materialModifiers: MaterialModifier, id: String, count: Int, mesh: MeshDeclaration, static: Boolean, transparent: Boolean, block: InstancedRenderablesContext.() -> Unit) {
        val rd = RenderableDeclaration(
                BaseMaterial.Renderable, materialModifiers.asList(),
                InstancedMesh(id, count, mesh, static, transparent, korenderContext.currentRetentionPolicy, block),
                transform = Transform(),
                korenderContext.currentRetentionPolicy
            )
        addToScene(transparent, rd)
    }

    override fun InstancedBillboards(vararg materialModifiers: MaterialModifier, id: String, count: Int, static: Boolean, transparent: Boolean, block: InstancedBillboardsContext.() -> Unit) {
        val rd = RenderableDeclaration(
                BaseMaterial.Billboard, materialModifiers.asList(),
                InstancedBillboard(id, count, static, transparent, korenderContext.currentRetentionPolicy, block),
                transform = Transform(),
                korenderContext.currentRetentionPolicy
            )
        addToScene(transparent, rd)
    }

    private fun addToScene(transparent: Boolean, rd: RenderableDeclaration) {
        if (transparent)
            sceneDeclaration.transparents += rd
        else
            sceneDeclaration.opaques += rd
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

    override fun PostProcess(vararg materialModifiers: MaterialModifier, block: FrameContext.() -> Unit) {
        val sd = SceneDeclaration()
        val fc = DefaultFrameContext(korenderContext, sd, frameInfo)
        fc.apply(block)
        sceneDeclaration.filters += InternalFilterDeclaration(materialModifiers.asList(), sd, korenderContext.currentRetentionPolicy)
    }

    override fun CaptureEnv(envProbeName: String, resolution: Int, position: Vec3, near: Float, far: Float, insideOut: Boolean, defs: Set<String>, block: FrameContext.() -> Unit) {
        val captureSceneDeclaration = SceneDeclaration()
        val envCaptureContext = EnvCaptureContext(resolution, position, near, far, insideOut, defs, captureSceneDeclaration)
        DefaultFrameContext(korenderContext, captureSceneDeclaration, frameInfo).apply(block)
        sceneDeclaration.envCaptures[envProbeName] = envCaptureContext
    }

    override fun CaptureFrame(frameProbeName: String, width: Int, height: Int, cameraDeclaration: CameraDeclaration, projectionDeclaration: ProjectionDeclaration, block: FrameContext.() -> Unit) {
        val captureSceneDeclaration = SceneDeclaration()
        val frameCaptureContext = FrameCaptureContext(width, height, cameraDeclaration as Camera, projectionDeclaration as Projection, captureSceneDeclaration)
        DefaultFrameContext(korenderContext, captureSceneDeclaration, frameInfo).apply(block)
        sceneDeclaration.frameCaptures[frameProbeName] = frameCaptureContext
    }
}