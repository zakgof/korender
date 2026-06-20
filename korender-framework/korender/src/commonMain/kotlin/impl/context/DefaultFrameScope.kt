package com.zakgof.korender.impl.context

import com.zakgof.korender.BaseMaterialScope
import com.zakgof.korender.BillboardMaterial
import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.CubeTextureDeclaration
import com.zakgof.korender.CubeTextureImages
import com.zakgof.korender.CubeTextureResources
import com.zakgof.korender.FrameInfo
import com.zakgof.korender.Image
import com.zakgof.korender.Image3D
import com.zakgof.korender.IndexType
import com.zakgof.korender.Material
import com.zakgof.korender.Mesh
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.MeshInitializer
import com.zakgof.korender.ModelInfo
import com.zakgof.korender.PostProcessingEffect
import com.zakgof.korender.PostProcessingMaterial
import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.ResourceTextureDeclaration
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.SkyMaterial
import com.zakgof.korender.TerrainMaterialScope
import com.zakgof.korender.Texture3DDeclaration
import com.zakgof.korender.TextureArrayDeclaration
import com.zakgof.korender.TextureArrayImages
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.impl.camera.Camera
import com.zakgof.korender.impl.engine.CaptureContext
import com.zakgof.korender.impl.engine.DeferredShadingDeclaration
import com.zakgof.korender.impl.engine.DirectionalLightDeclaration
import com.zakgof.korender.impl.engine.ElementDeclaration
import com.zakgof.korender.impl.engine.Engine
import com.zakgof.korender.impl.engine.FrameTarget
import com.zakgof.korender.impl.engine.HeightFieldDeclaration
import com.zakgof.korender.impl.engine.InternalBillboardInstancingDeclaration
import com.zakgof.korender.impl.engine.InternalFilterDeclaration
import com.zakgof.korender.impl.engine.InternalInstancingDeclaration
import com.zakgof.korender.impl.engine.InternalModelInstancingDeclaration
import com.zakgof.korender.impl.engine.InternalPassDeclaration
import com.zakgof.korender.impl.engine.ModelDeclaration
import com.zakgof.korender.impl.engine.PointLightDeclaration
import com.zakgof.korender.impl.engine.RegularFrameContext
import com.zakgof.korender.impl.engine.RenderableDeclaration
import com.zakgof.korender.impl.engine.SceneDeclaration
import com.zakgof.korender.impl.engine.ShadowDeclaration
import com.zakgof.korender.impl.geometry.InstancedBillboard
import com.zakgof.korender.impl.geometry.InstancedMesh
import com.zakgof.korender.impl.geometry.InternalBillboardInstancingParameter
import com.zakgof.korender.impl.geometry.InternalInstancingParameter
import com.zakgof.korender.impl.geometry.InternalMeshDeclaration
import com.zakgof.korender.impl.geometry.ScreenQuad
import com.zakgof.korender.impl.material.InternalBillboardMaterial
import com.zakgof.korender.impl.material.InternalMaterial
import com.zakgof.korender.impl.material.InternalPostProcessingMaterial
import com.zakgof.korender.impl.material.InternalSkyMaterial
import com.zakgof.korender.impl.projection.Projection
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Transform.Companion.IDENTITY
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.scope.BillboardInstancingDeclaration
import com.zakgof.korender.scope.DeferredShadingScope
import com.zakgof.korender.scope.FrameScope
import com.zakgof.korender.scope.GuiContainerScope
import com.zakgof.korender.scope.InstancingDeclaration
import com.zakgof.korender.scope.InstancingParameter
import com.zakgof.korender.scope.InstancingScope
import com.zakgof.korender.scope.KorenderScope
import com.zakgof.korender.scope.ModelInstancingDeclaration
import com.zakgof.korender.scope.PipeMeshScope
import com.zakgof.korender.scope.ResourceScope
import com.zakgof.korender.scope.ShadowScope

internal class DefaultFrameScope(
    val korenderContext: Engine.KorenderScopeImpl,
    val frameContext: RegularFrameContext,
    val sceneDeclaration: SceneDeclaration,
    override val frameInfo: FrameInfo,
    val nodeContext: NodeContext,
) : FrameScope, KorenderScope by korenderContext, ResourceScope {

    override var retentionPolicy: RetentionPolicy
        get() = nodeContext.retentionPolicy
        set(value) {
            nodeContext.retentionPolicy = value
        }

    override
    var camera: CameraDeclaration
        get() = frameContext.camera
        set(value) {
            frameContext.camera = value as Camera
        }

    override
    var projection: ProjectionDeclaration
        get() = frameContext.projection
        set(value) {
            frameContext.projection = value as Projection
        }

    override fun texture(textureResource: String, filter: TextureFilter, wrap: TextureWrap, aniso: Int): ResourceTextureDeclaration =
        nodeContext.texture(textureResource, filter, wrap, aniso)

    override fun texture(id: String, image: Image, filter: TextureFilter, wrap: TextureWrap, aniso: Int): TextureDeclaration =
        nodeContext.texture(id, image, filter, wrap, aniso)

    override fun texture(id: String, imageSupplier: () -> Image, filter: TextureFilter, wrap: TextureWrap, aniso: Int): TextureDeclaration =
        nodeContext.texture(id, imageSupplier, filter, wrap, aniso)

    override fun textureArray(vararg textureResources: String, filter: TextureFilter, wrap: TextureWrap, aniso: Int): TextureArrayDeclaration =
        nodeContext.textureArray(*textureResources, filter = filter, wrap = wrap, aniso = aniso)

    override fun textureArray(id: String, images: TextureArrayImages, filter: TextureFilter, wrap: TextureWrap, aniso: Int): TextureArrayDeclaration =
        nodeContext.textureArray(id, images, filter, wrap, aniso)

    override fun texture3D(id: String, image: Image3D, filter: TextureFilter, wrap: TextureWrap, aniso: Int): Texture3DDeclaration =
        nodeContext.texture3D(id, image, filter, wrap, aniso)

    override fun cubeTexture(resources: CubeTextureResources): CubeTextureDeclaration =
        nodeContext.cubeTexture(resources)

    override fun cubeTexture(id: String, images: CubeTextureImages): CubeTextureDeclaration =
        nodeContext.cubeTexture(id, images)

    override fun quad(halfSideX: Float, halfSideY: Float): MeshDeclaration =
        nodeContext.quad(halfSideX, halfSideY)

    override fun biQuad(halfSideX: Float, halfSideY: Float): MeshDeclaration =
        nodeContext.biQuad(halfSideX, halfSideY)

    override fun cube(halfSide: Float): MeshDeclaration =
        nodeContext.cube(halfSide)

    override fun sphere(radius: Float, slices: Int, sectors: Int): MeshDeclaration =
        nodeContext.sphere(radius, slices, sectors)

    override fun cylinderSide(height: Float, radius: Float, sectors: Int): MeshDeclaration =
        nodeContext.cylinderSide(height, radius, sectors)

    override fun coneTop(height: Float, radius: Float, sectors: Int): MeshDeclaration =
        nodeContext.coneTop(height, radius, sectors)

    override fun disk(radius: Float, sectors: Int): MeshDeclaration =
        nodeContext.disk(radius, sectors)

    override fun obj(objFile: String): MeshDeclaration =
        nodeContext.obj(objFile)

    override fun customMesh(
        id: String,
        vertexCount: Int,
        indexCount: Int,
        vararg attributes: MeshAttribute<*>,
        dynamic: Boolean,
        indexType: IndexType?,
        block: MeshInitializer.() -> Unit,
    ): MeshDeclaration =
        nodeContext.customMesh(id, vertexCount, indexCount, *attributes, dynamic = dynamic, indexType = indexType, block = block)

    override fun compositeMesh(
        id: String,
        prototypeMeshes: List<Pair<Mesh, Int>>,
        vararg attributes: MeshAttribute<*>,
        instancingParameters: Set<InstancingParameter>,
        dynamic: Boolean,
        block: InstancingScope.() -> Unit
    ): MeshDeclaration =
        nodeContext.compositeMesh(id, prototypeMeshes, *attributes, dynamic = dynamic, instancingParameters = instancingParameters, block = block)

    override fun heightField(id: String, cellsX: Int, cellsZ: Int, cellWidth: Float, height: (Int, Int) -> Float): MeshDeclaration =
        nodeContext.heightField(id, cellsX, cellsZ, cellWidth, height)

    override fun mesh(id: String, mesh: Mesh): MeshDeclaration =
        nodeContext.mesh(id, mesh)

    override fun pipeMesh(id: String, segments: Int, dynamic: Boolean, block: PipeMeshScope.() -> Unit): MeshDeclaration =
        nodeContext.pipeMesh(id, segments, dynamic, block)

    override fun DeferredShading(block: DeferredShadingScope.() -> Unit) {
        sceneDeclaration.deferredShadingDeclaration = DeferredShadingDeclaration(nodeContext)
        DefaultDeferredShadingScope(sceneDeclaration.deferredShadingDeclaration!!).apply(block)
    }

    override fun Model(resource: String, transform: Transform, instancing: ModelInstancingDeclaration?, animation: Int?, onUpdate: ((ModelInfo) -> Unit)?, materialModifier: BaseMaterialScope.() -> Unit) {
        sceneDeclaration.models += ModelDeclaration(
            resource,
            transform,
            instancing as InternalModelInstancingDeclaration?,
            nodeContext.time ?: frameInfo.time,
            animation ?: 0,
            onUpdate,
            materialModifier,
            nodeContext
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun Renderable(material: Material, mesh: MeshDeclaration, transform: Transform, transparent: Boolean, instancing: InstancingDeclaration?) {
        val meshDeclaration = (instancing as? InternalInstancingDeclaration)?.let {
            InstancedMesh(instancing.id, instancing.count, mesh, !instancing.dynamic, transparent, nodeContext, instancing.parameters as List<InternalInstancingParameter>, instancing.instancer)
        } ?: mesh
        val rd = RenderableDeclaration(material as InternalMaterial, meshDeclaration as InternalMeshDeclaration, transform, transparent, nodeContext)
        sceneDeclaration.append(rd)
    }

    @Suppress("UNCHECKED_CAST")
    override fun Billboard(material: BillboardMaterial, transparent: Boolean, instancing: BillboardInstancingDeclaration?) {
        val mesh = com.zakgof.korender.impl.geometry.Billboard(nodeContext)
        val meshDeclaration = if (instancing != null) {
            instancing as InternalBillboardInstancingDeclaration
            InstancedBillboard(instancing.id, instancing.count, !instancing.dynamic, transparent, nodeContext, instancing.parameters as List<InternalBillboardInstancingParameter>, instancing.instancer)
        } else {
            mesh
        }
        val rd = RenderableDeclaration(
            material as InternalBillboardMaterial,
            meshDeclaration,
            IDENTITY,
            transparent,
            nodeContext
        )
        sceneDeclaration.append(rd)
    }

    override fun Sky(material: SkyMaterial) {
        sceneDeclaration.skies += RenderableDeclaration(material as InternalSkyMaterial, ScreenQuad(nodeContext), Transform.IDENTITY, false, nodeContext)
    }

    override fun Gui(block: GuiContainerScope.() -> Unit) {
        val root = ElementDeclaration.Container(Direction.Vertical)
        DefaultContainerScope(this, root).apply(block)
        sceneDeclaration.guis += root
    }

    override fun DirectionalLight(direction: Vec3, color: ColorRGB, block: ShadowScope.() -> Unit) {
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

    override fun PostProcess(postProcessingEffect: PostProcessingEffect, block: FrameScope.() -> Unit) {
        val sd = SceneDeclaration()
        val fc = DefaultFrameScope(korenderContext, korenderContext.regularFrameContext, sd, frameInfo, nodeContext)
        fc.apply(block)
        sceneDeclaration.filters += postProcessingEffect as InternalFilterDeclaration
    }

    override fun PostProcess(material: PostProcessingMaterial, block: FrameScope.() -> Unit) {
        val sd = SceneDeclaration()
        val fc = DefaultFrameScope(korenderContext, korenderContext.regularFrameContext, sd, frameInfo, nodeContext)
        fc.apply(block)
        sceneDeclaration.filters += InternalFilterDeclaration(
            listOf(
                InternalPassDeclaration(
                    mapping = mapOf(),
                    material = material as InternalPostProcessingMaterial,
                    sceneDeclaration = sd,
                    target = FrameTarget("colorTexture", "depthTexture"),
                    nodeContext = nodeContext
                )
            )
        )
    }

    override fun CaptureEnv(envProbeName: String, resolution: Int, block: FrameScope.() -> Unit) {
        val captureSceneDeclaration = SceneDeclaration()
        val rfc = RegularFrameContext(resolution, resolution, korenderContext.renderContext)
        val captureContext = CaptureContext(rfc, captureSceneDeclaration, nodeContext)
        DefaultFrameScope(korenderContext, rfc, captureSceneDeclaration, frameInfo, nodeContext).apply(block)
        sceneDeclaration.envCaptures[envProbeName] = captureContext
    }

    override fun CaptureFrame(frameProbeName: String, width: Int, height: Int, block: FrameScope.() -> Unit) {
        val captureSceneDeclaration = SceneDeclaration()
        val rfc = RegularFrameContext(width, height, korenderContext.renderContext)
        val captureContext = CaptureContext(rfc, captureSceneDeclaration, nodeContext)
        DefaultFrameScope(korenderContext, rfc, captureSceneDeclaration, frameInfo, nodeContext).apply(block)
        sceneDeclaration.frameCaptures[frameProbeName] = captureContext
    }

    override fun OnLoading(force: Boolean, block: FrameScope.() -> Unit) {
        sceneDeclaration.loaderSceneDeclaration = SceneDeclaration()
        sceneDeclaration.loaderForced = force
        DefaultFrameScope(korenderContext, korenderContext.regularFrameContext, sceneDeclaration.loaderSceneDeclaration!!, frameInfo, nodeContext).apply(block)
    }

    override fun Node(resourceLoader: (suspend (String) -> ByteArray)?, transform: Transform, retentionPolicy: RetentionPolicy?, time: Float?, block: FrameScope.() -> Unit) {

        val childNodeContext = NodeContext(
            resourceLoader ?: nodeContext.resourceLoader,
            transform * nodeContext.transform,
            retentionPolicy ?: nodeContext.retentionPolicy,
            time
        )
        DefaultFrameScope(korenderContext, korenderContext.regularFrameContext, sceneDeclaration, frameInfo, childNodeContext).apply(block)
    }

    override fun HeightField(id: String, cellSize: Float, hg: Int, rings: Int, block: TerrainMaterialScope.() -> Unit) {
        sceneDeclaration.heightFields += HeightFieldDeclaration(id, cellSize, hg, rings, block, this)
    }
}
