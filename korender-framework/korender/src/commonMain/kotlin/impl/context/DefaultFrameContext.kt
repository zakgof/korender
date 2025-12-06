package com.zakgof.korender.impl.context

import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.FrameInfo
import com.zakgof.korender.GltfModel
import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.PostProcessingEffect
import com.zakgof.korender.Prefab
import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.context.BillboardInstancingDeclaration
import com.zakgof.korender.context.DeferredShadingContext
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.context.GltfInstancingDeclaration
import com.zakgof.korender.context.GuiContainerContext
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
import com.zakgof.korender.impl.engine.FrameTarget
import com.zakgof.korender.impl.engine.GltfDeclaration
import com.zakgof.korender.impl.engine.InternalBillboardInstancingDeclaration
import com.zakgof.korender.impl.engine.InternalFilterDeclaration
import com.zakgof.korender.impl.engine.InternalGltfInstancingDeclaration
import com.zakgof.korender.impl.engine.InternalInstancingDeclaration
import com.zakgof.korender.impl.engine.InternalPassDeclaration
import com.zakgof.korender.impl.engine.PointLightDeclaration
import com.zakgof.korender.impl.engine.RenderableDeclaration
import com.zakgof.korender.impl.engine.SceneDeclaration
import com.zakgof.korender.impl.engine.ShadowDeclaration
import com.zakgof.korender.impl.geometry.InstancedBillboard
import com.zakgof.korender.impl.geometry.InstancedMesh
import com.zakgof.korender.impl.geometry.ScreenQuad
import com.zakgof.korender.impl.material.InternalMaterialModifier
import com.zakgof.korender.impl.prefab.InternalPrefab
import com.zakgof.korender.impl.projection.Projection
import com.zakgof.korender.impl.resourceBytes
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Transform.Companion.IDENTITY
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

    override fun Gltf(resource: String, transform: Transform, time: Float?, animation: Int?, instancing: GltfInstancingDeclaration?) {
        sceneDeclaration.gltfs += GltfDeclaration(
            resource,
            { resourceBytes(korenderContext.appResourceLoader, resource) },
            {},
            transform,
            time ?: frameInfo.time,
            animation ?: 0,
            instancing as InternalGltfInstancingDeclaration?,
            korenderContext.currentRetentionPolicy
        )
    }

    override fun Gltf(id: String, bytes: ByteArray, transform: Transform, time: Float?, animation: Int?, instancing: GltfInstancingDeclaration?, onLoaded: (GltfModel) -> Unit) {
        sceneDeclaration.gltfs += GltfDeclaration(id, { bytes }, onLoaded, transform, time ?: frameInfo.time, animation ?: 0, instancing as InternalGltfInstancingDeclaration?, korenderContext.currentRetentionPolicy)
    }

    override fun Renderable(vararg materialModifiers: MaterialModifier, mesh: MeshDeclaration, transform: Transform, transparent: Boolean, instancing: InstancingDeclaration?) {
        val meshDeclaration = (instancing as? InternalInstancingDeclaration)?.let {
            InstancedMesh(instancing.id, instancing.count, mesh, !instancing.dynamic, transparent, korenderContext.currentRetentionPolicy, instancing.instancer)
        } ?: mesh
        val rd = RenderableDeclaration(BaseMaterial.Renderable, materialModifiers.asList(), meshDeclaration, transform, transparent, korenderContext.currentRetentionPolicy)
        sceneDeclaration.append(rd)
    }

    override fun Renderable(vararg materialModifiers: MaterialModifier, prefab: Prefab) {
        (prefab as InternalPrefab).render(this, *materialModifiers)
    }

    override fun Billboard(vararg materialModifiers: MaterialModifier, transparent: Boolean, instancing: BillboardInstancingDeclaration?) {
        val mesh = com.zakgof.korender.impl.geometry.Billboard(korenderContext.currentRetentionPolicy)
        val meshDeclaration = if (instancing != null) {
            instancing as InternalBillboardInstancingDeclaration
            InstancedBillboard(instancing.id, instancing.count, !instancing.dynamic, transparent, korenderContext.currentRetentionPolicy, instancing.instancer)
        } else {
            mesh
        }
        val rd = RenderableDeclaration(
            BaseMaterial.Billboard,
            materialModifiers.asList(),
            meshDeclaration,
            IDENTITY,
            transparent,
            korenderContext.currentRetentionPolicy
        )
        sceneDeclaration.append(rd)
    }

    override fun Sky(vararg materialModifiers: MaterialModifier) {
        sceneDeclaration.skies += RenderableDeclaration(BaseMaterial.Sky, materialModifiers.asList(), ScreenQuad(korenderContext.currentRetentionPolicy), Transform.IDENTITY, false, korenderContext.currentRetentionPolicy)
    }

    override fun Gui(block: GuiContainerContext.() -> Unit) {
        val root = ElementDeclaration.Container(Direction.Vertical)
        DefaultContainerContext(this, root).apply(block)
        sceneDeclaration.guis += root
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

    override fun PostProcess(postProcessingEffect: PostProcessingEffect, block: FrameContext.() -> Unit) {
        val sd = SceneDeclaration()
        val fc = DefaultFrameContext(korenderContext, sd, frameInfo)
        fc.apply(block)
        sceneDeclaration.filters += postProcessingEffect as InternalFilterDeclaration
    }

    override fun PostProcess(vararg materialModifiers: MaterialModifier, block: FrameContext.() -> Unit) {
        val sd = SceneDeclaration()
        val fc = DefaultFrameContext(korenderContext, sd, frameInfo)
        fc.apply(block)
        sceneDeclaration.filters += InternalFilterDeclaration(
            listOf(
                InternalPassDeclaration(
                    mapping = mapOf(),
                    modifiers = materialModifiers.asList().map { it as InternalMaterialModifier },
                    sceneDeclaration = sd,
                    target = FrameTarget(fc.width, fc.height, "colorTexture", "depthTexture"),
                    retentionPolicy = korenderContext.currentRetentionPolicy
                )
            )
        )
    }

    override fun CaptureEnv(envProbeName: String, resolution: Int, position: Vec3, near: Float, far: Float, insideOut: Boolean, block: FrameContext.() -> Unit) {
        val captureSceneDeclaration = SceneDeclaration()
        val envCaptureContext = EnvCaptureContext(resolution, position, near, far, insideOut, captureSceneDeclaration)
        DefaultFrameContext(korenderContext, captureSceneDeclaration, frameInfo).apply(block)
        sceneDeclaration.envCaptures[envProbeName] = envCaptureContext
    }

    override fun CaptureFrame(frameProbeName: String, width: Int, height: Int, camera: CameraDeclaration, projection: ProjectionDeclaration, block: FrameContext.() -> Unit) {
        val captureSceneDeclaration = SceneDeclaration()
        val frameCaptureContext = FrameCaptureContext(width, height, camera as Camera, projection as Projection, captureSceneDeclaration)
        DefaultFrameContext(korenderContext, captureSceneDeclaration, frameInfo).apply(block)
        sceneDeclaration.frameCaptures[frameProbeName] = frameCaptureContext
    }

    override fun OnLoading(block: FrameContext.() -> Unit) {
        sceneDeclaration.loaderSceneDeclaration = SceneDeclaration()
        DefaultFrameContext(korenderContext, sceneDeclaration.loaderSceneDeclaration!!, frameInfo).apply(block)
    }
}