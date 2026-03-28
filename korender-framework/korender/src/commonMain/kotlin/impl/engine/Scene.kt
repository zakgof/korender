package com.zakgof.korender.impl.engine

import com.zakgof.korender.ProjectionMode
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.impl.camera.Camera
import com.zakgof.korender.impl.camera.DefaultCamera
import com.zakgof.korender.impl.engine.shadow.CustomFrameContext
import com.zakgof.korender.impl.engine.shadow.ShadowRenderer
import com.zakgof.korender.impl.engine.shadow.ShadowerData
import com.zakgof.korender.impl.geometry.DecalCube
import com.zakgof.korender.impl.geometry.Instanceable
import com.zakgof.korender.impl.geometry.InternalMeshDeclaration
import com.zakgof.korender.impl.geometry.ScreenQuad
import com.zakgof.korender.impl.gl.GL.glClear
import com.zakgof.korender.impl.gl.GL.glViewport
import com.zakgof.korender.impl.gl.GLConstants.GL_COLOR_BUFFER_BIT
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_BUFFER_BIT
import com.zakgof.korender.impl.gl.GLConstants.GL_FRONT
import com.zakgof.korender.impl.gl.GLConstants.GL_GEQUAL
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP_NEGATIVE_X
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP_POSITIVE_X
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP_POSITIVE_Y
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP_POSITIVE_Z
import com.zakgof.korender.impl.glgpu.Color3ListGetter
import com.zakgof.korender.impl.glgpu.Color4ListGetter
import com.zakgof.korender.impl.glgpu.ColorRGBGetter
import com.zakgof.korender.impl.glgpu.FloatListGetter
import com.zakgof.korender.impl.glgpu.GLBindableTexture
import com.zakgof.korender.impl.glgpu.GlGpuCubeTexture
import com.zakgof.korender.impl.glgpu.GlGpuShadowTextureList
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.glgpu.GlGpuTextureList
import com.zakgof.korender.impl.glgpu.IntGetter
import com.zakgof.korender.impl.glgpu.IntListGetter
import com.zakgof.korender.impl.glgpu.Mat4Getter
import com.zakgof.korender.impl.glgpu.Mat4ListGetter
import com.zakgof.korender.impl.glgpu.ShadowTextureListGetter
import com.zakgof.korender.impl.glgpu.TextureListGetter
import com.zakgof.korender.impl.glgpu.UniformGetter
import com.zakgof.korender.impl.glgpu.Vec3ListGetter
import com.zakgof.korender.impl.gltf.GltfSceneBuilder
import com.zakgof.korender.impl.material.DecalBlendMaterial
import com.zakgof.korender.impl.material.ImageCubeTextureDeclaration
import com.zakgof.korender.impl.material.ImageTexture3DDeclaration
import com.zakgof.korender.impl.material.InternalMaterial
import com.zakgof.korender.impl.material.InternalMaterialModifier
import com.zakgof.korender.impl.material.InternalPostShadingEffect
import com.zakgof.korender.impl.material.InternalTexture
import com.zakgof.korender.impl.material.NotYetLoadedTexture
import com.zakgof.korender.impl.material.ProbeCubeTextureDeclaration
import com.zakgof.korender.impl.material.ProbeTextureDeclaration
import com.zakgof.korender.impl.material.ResourceCubeTextureDeclaration
import com.zakgof.korender.impl.projection.FrustumProjectionMode
import com.zakgof.korender.impl.projection.LogProjectionMode
import com.zakgof.korender.impl.projection.OrthoProjectionMode
import com.zakgof.korender.impl.projection.Projection
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

internal class Scene(
    private val sceneDeclaration: SceneDeclaration,
    val inventory: Inventory,
    val renderContext: RenderContext,
    val currentRetentionPolicy: RetentionPolicy,
) {

    class LightMaterialModifier(private val sc: SceneDeclaration) : InternalMaterialModifier() {

        private val directionalLightsDirs = sc.directionalLights.map { it.direction }
        private val directionalLightsColors = sc.directionalLights.map { it.color }
        var dlsti = List(32) { 0 }
        var dlstc = List(32) { 0 }
        var numShadows = 0
        var bsps = listOf<Mat4>()
        var cascades = listOf<ColorRGBA>()
        var yMins = listOf<Float>()
        var yMaxs = listOf<Float>()
        var shadowModes = listOf<Int>()
        var i1 = listOf<Int>()
        var f1 = listOf<Float>()
        var f2 = listOf<Float>()

        override fun uniform(name: String): UniformGetter<*>? =
            when (name) {
                // TODO move ALL composites to vals
                "numDirectionalLights" -> IntGetter<LightMaterialModifier> { it.sc.directionalLights.size }
                "directionalLightDir[0]" -> Vec3ListGetter<LightMaterialModifier> { it.directionalLightsDirs }
                "directionalLightColor[0]" -> Color3ListGetter<LightMaterialModifier> { it.directionalLightsColors }
                "directionalLightShadowTextureIndex[0]" -> IntListGetter<LightMaterialModifier> { it.dlsti }
                "directionalLightShadowTextureCount[0]" -> IntListGetter<LightMaterialModifier> { it.dlstc }
                "ambientColor" -> ColorRGBGetter<LightMaterialModifier> { it.sc.ambientLightColor }
                "numPointLights" -> IntGetter<LightMaterialModifier> { it.sc.pointLights.size }
                "pointLightPos[0]" -> Vec3ListGetter<LightMaterialModifier> { it.sc.pointLights.map { it.position } }
                "pointLightColor[0]" -> Color3ListGetter<LightMaterialModifier> { it.sc.pointLights.map { it.color } }
                "pointLightAttenuation[0]" -> Vec3ListGetter<LightMaterialModifier> { it.sc.pointLights.map { it.attenuation } }
                "numShadows" -> IntGetter<LightMaterialModifier> { it.numShadows }
                "bsps[0]" -> Mat4ListGetter<LightMaterialModifier> { it.bsps }
                "cascade[0]" -> Color4ListGetter<LightMaterialModifier> { it.cascades }
                "yMin[0]" -> FloatListGetter<LightMaterialModifier> { it.yMins }
                "yMax[0]" -> FloatListGetter<LightMaterialModifier> { it.yMaxs }
                "shadowMode[0]" -> IntListGetter<LightMaterialModifier> { it.shadowModes }
                "i1[0]" -> IntListGetter<LightMaterialModifier> { it.i1 }
                "f1[0]" -> FloatListGetter<LightMaterialModifier> { it.f1 }
                "f2[0]" -> FloatListGetter<LightMaterialModifier> { it.f2 }
                else -> super.uniform(name)
            }
    }

    class ContextMaterialModifier(private val renderContext: RenderContext) : InternalMaterialModifier() {

        var shadowTextures = GlGpuTextureList(List(5) { null }, 5)
        var pcfTextures = GlGpuShadowTextureList(List(5) { null }, 5)

        override fun uniform(name: String): UniformGetter<*>? =
            when (name) {
                "noiseTexture" -> noiseTexGetter
                "fbmTexture" -> fbmTexGetter
                "shadowTextures[0]" -> TextureListGetter<ContextMaterialModifier> { it.shadowTextures }
                "pcfTextures[0]" -> ShadowTextureListGetter<ContextMaterialModifier> { it.pcfTextures }
                else -> super.uniform(name)
            }

        override val plugins
            get() = super.plugins + listOfNotNull(
                "vprojection" to renderContext.projection.mode.plugin(),
                (renderContext.projection.mode as? LogProjectionMode)?.let {
                    "depth" to "!shader/plugin/depth.log.frag"
                }
            )

        private fun ProjectionMode.plugin() = when (this) {
            is FrustumProjectionMode -> "!shader/plugin/vprojection.frustum.vert"
            is OrthoProjectionMode -> "!shader/plugin/vprojection.ortho.vert"
            is LogProjectionMode -> "!shader/plugin/vprojection.log.vert"
            else -> ""
        }
    }

    private val deferredShading = sceneDeclaration.deferredShadingDeclaration != null
    private val reusableFrameBufferHolder = ReusableFrameBufferHolder()

    val lightMaterialModifier = LightMaterialModifier(sceneDeclaration)
    val contextMaterialModifier = ContextMaterialModifier(renderContext)

    val touchBoxes = mutableListOf<TouchBox>()

    // TODO: introduce super-generic texture declaration interface
    private val fixer: (Any?) -> GLBindableTexture = { value: Any? ->
        when (value) {
            is InternalTexture -> inventory.texture(value) ?: NotYetLoadedTexture
            is ProbeTextureDeclaration -> renderContext.frameProbes[value.frameProbeName] ?: NotYetLoadedTexture
            is ResourceCubeTextureDeclaration -> inventory.cubeTexture(value) ?: NotYetLoadedTexture
            is ImageCubeTextureDeclaration -> inventory.cubeTexture(value) ?: NotYetLoadedTexture
            is ProbeCubeTextureDeclaration -> renderContext.envProbes[value.envProbeName] ?: NotYetLoadedTexture
            is ImageTexture3DDeclaration -> inventory.texture3D(value) ?: NotYetLoadedTexture
            null -> null
            else -> value
        } as GLBindableTexture
    }

    init {
        sceneDeclaration.gltfs.forEach {
            inventory.gltf(it)?.let { gltfLoaded ->
                GltfSceneBuilder(it, gltfLoaded).build().forEach { rd -> sceneDeclaration.append(rd) }
            }
        }
    }

    fun render(): Boolean {

        renderEnvProbes()
        renderFrameProbes()

        inventory.uniformBufferHolder.populateFrame(listOf(renderContext.frameMaterialModifier, lightMaterialModifier), true)

        try {
            renderShadows()
            if (deferredShading) {
                renderSceneDeferred()
            } else {
                renderSceneForward()
            }
            return true
        } catch (sr: SkipRender) {
            println("Scene rendering skipped as resource not ready: [${sr.text}]")
            return false
        }
    }

    private fun renderEnvProbes() {
        sceneDeclaration.envCaptures.forEach { kv ->
            try {
                Scene(kv.value.sceneDeclaration, inventory, renderContext, currentRetentionPolicy)
                    .renderToEnvProbe(kv.value, kv.key)
                    ?.let {
                        renderContext.envProbes[kv.key] = it
                    }
            } catch (sr: SkipRender) {
                println("Env probing skipped as resource not ready: [${sr.text}]")
                return
            }
        }
    }

    private fun renderFrameProbes() {
        sceneDeclaration.frameCaptures.forEach { kv ->
            try {
                Scene(kv.value.sceneDeclaration, inventory, renderContext, currentRetentionPolicy)
                    .renderToFrameProbe(kv.value, kv.key)
                    ?.let {
                        renderContext.frameProbes[kv.key] = it
                    }
            } catch (sr: SkipRender) {
                println("Frame probing skipped as resource not ready: [${sr.text}]")
                return
            }
        }
    }

    private fun renderSceneDeferred() {

        renderDeferredOpaques()

        val postShadingEffects = sceneDeclaration.deferredShadingDeclaration!!.postShadingEffects.map { it as InternalPostShadingEffect }
        renderDeferredShading(postShadingEffects.isNotEmpty() || sceneDeclaration.filters.isNotEmpty())

        if (postShadingEffects.isNotEmpty()) {
            postShadingEffects.forEach {
                renderPostShadingEffect(it)
            }
            renderComposition(sceneDeclaration.filters.isNotEmpty())
            postShadingEffects.flatMap { it.keepTextures }
                .forEach { reusableFrameBufferHolder.unlock(it) }
        }
        renderPostProcess()
        // TODO fog over transparents ??
        renderTransparents(sceneDeclaration, renderContext.camera)
    }

    private fun renderDeferredShading(reusable: Boolean) {
        val shadingMaterial = InternalMaterial("!shader/screen.vert", "!shader/deferred/shading.frag")
        val shadingMaterialDeclaration = shadingMaterial.toDeclaration(true, currentRetentionPolicy, listOf(contextMaterialModifier))
        val target = if (reusable) renderContext.defaultTarget() else null
        renderToReusableFb(target) {
            renderFullscreen(shadingMaterialDeclaration)
            renderBucket(sceneDeclaration.skies)
        }
    }

    private fun renderPostShadingEffect(effect: InternalPostShadingEffect) {
        effect.effectPasses.forEach { pass ->
            renderToReusableFb(pass.target) {
                pass.mapping.forEach {
                    contextMaterialModifier.customTextureUniforms[it.key] = contextMaterialModifier.customTextureUniforms[it.value]!!
                }
                val materialDeclaration = pass.material.toDeclaration(
                    true, currentRetentionPolicy,
                    listOf(contextMaterialModifier)
                )
                renderFullscreen(materialDeclaration, pass.target.width, pass.target.height)
            }
        }
        effect.effectPasses.flatMap { listOf(it.target.colorOutput, it.target.depthOutput) }
            .filter { !effect.keepTextures.contains(it) }
            .forEach { reusableFrameBufferHolder.unlock(it) }
    }

    private fun renderComposition(reusable: Boolean) {
        val compositionModifiers = listOf(contextMaterialModifier) + sceneDeclaration.deferredShadingDeclaration!!.postShadingEffects
            .map { it as InternalPostShadingEffect }
            .map { it.compositionMaterialModifier }
        val compositionMaterial = InternalMaterial("!shader/screen.vert", "!shader/deferred/composition.frag")

        val compositionMaterialDeclaration = compositionMaterial.toDeclaration(true, currentRetentionPolicy, compositionModifiers)
        val target = if (reusable) renderContext.defaultTarget() else null
        renderToReusableFb(target) {
            renderFullscreen(compositionMaterialDeclaration)
        }
    }

    private fun renderSceneForward() {
        val target = if (sceneDeclaration.filters.isNotEmpty()) renderContext.defaultTarget() else null
        renderToReusableFb(target) {
            prepareScene()
            renderForwardOpaques(sceneDeclaration)
        }
        renderPostProcess()
        // TODO: fog over transparents !!!
        renderTransparents(sceneDeclaration, renderContext.camera)
    }

    private fun renderPostProcess() {
        val passes = sceneDeclaration.filters.flatMap { it.passes }
        if (passes.isNotEmpty()) {
            passes.dropLast(1).forEach { pass ->
                renderToReusableFb(pass.target) {
                    renderPostProcessPass(pass)
                }
            }
            renderPostProcessPass(passes.last())
        }
    }

    private fun renderDeferredOpaques() {
        val geometryBuffer = inventory.frameBuffer(
            FrameBufferDeclaration(
                "geometry", renderContext.width, renderContext.height,
                listOf(
                    GlGpuTexture.Preset.RGBAFilter, // TODO review
                    GlGpuTexture.Preset.Normal,
                    GlGpuTexture.Preset.RGBAFilter,
                ),
                true, TransientProperty(currentRetentionPolicy)
            )
        ) ?: throw SkipRender("Geometry FB")
        geometryBuffer.exec {
            renderContext.state.set {
                blend(false)
            }
            glViewport(0, 0, renderContext.width, renderContext.height)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            renderBucket(sceneDeclaration.opaques)
        }
        // TODO: vars !!!
        contextMaterialModifier.customTextureUniforms["albedoGeometryTexture"] = geometryBuffer.colorTextures[0]
        contextMaterialModifier.customTextureUniforms["normalGeometryTexture"] = geometryBuffer.colorTextures[1]
        contextMaterialModifier.customTextureUniforms["emissionGeometryTexture"] = geometryBuffer.colorTextures[2]
        contextMaterialModifier.customTextureUniforms["depthGeometryTexture"] = geometryBuffer.depthTexture!!
        renderDecals()
    }

    private fun renderDecals() {
        if (sceneDeclaration.deferredShadingDeclaration!!.decals.isNotEmpty()) {
            // TODO: downscaled
            val decalsFb = inventory.frameBuffer(
                FrameBufferDeclaration(
                    "decals", renderContext.width, renderContext.height,
                    listOf(GlGpuTexture.Preset.RGBAFilter, GlGpuTexture.Preset.RGBAFilter, GlGpuTexture.Preset.RGBAFilter),
                    true, TransientProperty(currentRetentionPolicy)
                )
            )
            // TODO: no depth on blend
            val decalBlendFb = inventory.frameBuffer(
                FrameBufferDeclaration(
                    "decal-blend", renderContext.width, renderContext.height,
                    listOf(GlGpuTexture.Preset.RGBAFilter, GlGpuTexture.Preset.RGBAFilter), false, TransientProperty(currentRetentionPolicy)
                )
            )

            if (decalsFb != null && decalBlendFb != null) {
                decalsFb.exec {
                    renderContext.state.set {
                        clearColor(ColorRGBA(0f, 0f, 0f, 0f))
                        depthTest(false)
                    }
                    glViewport(0, 0, renderContext.width, renderContext.height)
                    glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

                    sceneDeclaration.deferredShadingDeclaration!!.decals.forEach { decalDeclaration ->

                        val right = decalDeclaration.look % decalDeclaration.up
                        val model = Mat4(
                            right.x, right.y, right.z, decalDeclaration.position.x,
                            decalDeclaration.up.x, decalDeclaration.up.y, decalDeclaration.up.z, decalDeclaration.position.y,
                            decalDeclaration.look.x, decalDeclaration.look.y, decalDeclaration.look.z, decalDeclaration.position.z,
                            0f, 0f, 0f, 1f
                        ) * scale(decalDeclaration.size).mat4

                        val renderableDeclaration = RenderableDeclaration(decalDeclaration.material, listOf(), DecalCube(0.5f, currentRetentionPolicy), Transform(model), true, currentRetentionPolicy)
                        renderRenderable(renderableDeclaration, renderContext.camera)
                    }
                    inventory.uniformBufferHolder.flush()
                }
                decalBlendFb.exec {
                    val decalBlendMaterial = DecalBlendMaterial(decalsFb.colorTextures[0], decalsFb.colorTextures[1])
                    val decalBlendMaterialDeclaration = decalBlendMaterial.toDeclaration(
                        true,
                        currentRetentionPolicy,
                        listOf(contextMaterialModifier)
                    )
                    renderFullscreen(decalBlendMaterialDeclaration) { blend(false) }
                }
                // TODO: vars !!!
                contextMaterialModifier.customTextureUniforms["albedoGeometryTexture"] = decalBlendFb.colorTextures[0]
                contextMaterialModifier.customTextureUniforms["normalGeometryTexture"] = decalBlendFb.colorTextures[1]
            }
        }
    }

    private fun renderBucket(renderables: List<RenderableDeclaration>): Boolean {
        var success = true
        renderables.forEach {
            success = success and renderRenderable(it, renderContext.camera)
        }
        return success and inventory.uniformBufferHolder.flush()
    }

    private fun prepareScene(
        w: Int = renderContext.width,
        h: Int = renderContext.height,
        insideOut: Boolean = false,
    ) {
        renderContext.state.set {
            clearColor(renderContext.backgroundColor)
            if (insideOut) {
                cullFaceMode(GL_FRONT)
                depthFunc(GL_GEQUAL)
                clearDepth(0.0f)
            }
        }
        glViewport(0, 0, w, h)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
    }

    private fun renderForwardOpaques(sceneDeclaration: SceneDeclaration): Boolean {
        var success = true
        success = success and renderBucket(sceneDeclaration.opaques)
        success = success and renderBucket(sceneDeclaration.skies)
        return success
    }

    private fun renderTransparents(
        sceneDeclaration: SceneDeclaration,
        camera: Camera,
        insideOut: Boolean = false,
        width: Int = renderContext.width,
        height: Int = renderContext.height,
    ): Boolean {
        var success = true
        renderContext.state.set {
            depthMask(false)
            if (insideOut) {
                cullFaceMode(GL_FRONT)
                depthFunc(GL_GEQUAL)
            }
        }
        val reverse = if (insideOut) -1f else 1f
        sceneDeclaration.transparents
            .sortedByDescending { (camera.mat4 * it.transform.offset()).z * reverse }
            .forEach {
                success = success and renderRenderable(it, camera, insideOut)
            }
        success = success and inventory.uniformBufferHolder.flush()

        val guiRenderers = sceneDeclaration.guis.map {
            GuiRenderer(inventory, width, height, it)
        }
        touchBoxes += guiRenderers.flatMap { it.touchBoxes }
        guiRenderers.flatMap { it.renderableDeclarations }
            .forEach {
                success = success and renderRenderable(it, renderContext.camera)
            }
        success = success and inventory.uniformBufferHolder.flush()
        return success
    }

    private fun renderShadows() {
        val shadowData = mutableListOf<ShadowerData>()
        val directionalShadowIndexes = mutableListOf<Int>()
        val directionalShadowCounts = mutableListOf<Int>()
        sceneDeclaration.directionalLights.forEachIndexed { li, dl ->
            val indexes = dl.shadowDeclaration.cascades.mapIndexedNotNull { ci, cascadeDeclaration ->
                ShadowRenderer.render(
                    "$li-$ci",
                    dl.direction,
                    dl.shadowDeclaration.cascades,
                    ci,
                    sceneDeclaration.opaques + sceneDeclaration.transparents,
                    this
                )?.let {
                    shadowData += it
                    shadowData.size - 1
                }
            }
            directionalShadowIndexes += indexes.minOrNull() ?: -1
            directionalShadowCounts += indexes.size
        }
        lightMaterialModifier.numShadows = shadowData.size
        lightMaterialModifier.bsps = shadowData.map { it.bsp }
        lightMaterialModifier.cascades = shadowData.map { ColorRGBA(it.cascade[0], it.cascade[1], it.cascade[2], it.cascade[3]) }
        lightMaterialModifier.cascades = shadowData.map { ColorRGBA(it.cascade[0], it.cascade[1], it.cascade[2], it.cascade[3]) }
        lightMaterialModifier.yMins = shadowData.map { it.yMin }
        lightMaterialModifier.yMaxs = shadowData.map { it.yMax }
        lightMaterialModifier.shadowModes = shadowData.map { it.mode }
        lightMaterialModifier.i1 = shadowData.map { it.i1 }
        lightMaterialModifier.f1 = shadowData.map { it.f1 }
        lightMaterialModifier.f2 = shadowData.map { it.f2 }
        contextMaterialModifier.shadowTextures = GlGpuTextureList(shadowData.map { it.texture }, 5)
        contextMaterialModifier.pcfTextures = GlGpuShadowTextureList(shadowData.map { it.pcfTexture }, 5)
        lightMaterialModifier.dlsti = directionalShadowIndexes
        lightMaterialModifier.dlstc = directionalShadowCounts
        inventory.uniformBufferHolder.populateFrame(listOf(renderContext.frameMaterialModifier, lightMaterialModifier), true)
    }

    private fun renderPostProcessPass(pass: InternalPassDeclaration) {
        contextMaterialModifier.customTextureUniforms["colorInputTexture"] = contextMaterialModifier.customTextureUniforms["colorTexture"]!!
        contextMaterialModifier.customTextureUniforms["depthInputTexture"] = contextMaterialModifier.customTextureUniforms["depthTexture"]!!
        pass.mapping.forEach {
            contextMaterialModifier.customTextureUniforms[it.key] = contextMaterialModifier.customTextureUniforms[it.value]!!
        }
        val passMaterialDeclaration = pass.material.toDeclaration(deferredShading, pass.retentionPolicy, listOf(contextMaterialModifier))
        renderFullscreen(passMaterialDeclaration, pass.target.width, pass.target.height)
        pass.sceneDeclaration?.let { renderForwardOpaques(it) }
    }

    private fun renderFullscreen(
        quadMaterial: ShaderDeclaration,
        width: Int = renderContext.width,
        height: Int = renderContext.height,
        state: GlState.StateContext.() -> Unit = {},
    ) {
        val mesh = inventory.mesh(ScreenQuad(currentRetentionPolicy))
        val shader = inventory.shader(quadMaterial)
        renderContext.state.set {
            blend(false)
            state()
        }
        glViewport(0, 0, width, height)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        if (mesh != null && shader != null) {
            shader.render(quadMaterial.uniformSuppliers, fixer, mesh.gpuMesh)
            inventory.uniformBufferHolder.flush()
        }
    }

    private fun renderToReusableFb(target: FrameTarget?, block: () -> Unit) {
        if (target == null) {
            block()
        } else {
            val fbDeclaration = reusableFrameBufferHolder.request(target, currentRetentionPolicy)
            val fb = inventory.frameBuffer(fbDeclaration)
                ?: throw SkipRender("Reusable FB '${fbDeclaration.id}'")
            fb.exec { block() }
            contextMaterialModifier.customTextureUniforms[target.colorOutput] = fb.colorTextures[0]
            contextMaterialModifier.customTextureUniforms[target.depthOutput] = fb.depthTexture!!
        }
    }

    fun renderToEnvProbe(envCaptureContext: EnvCaptureContext, probeName: String): GlGpuCubeTexture? {
        var success = true
        val probeFb =
            inventory.cubeFrameBuffer(CubeFrameBufferDeclaration("probe-$probeName", envCaptureContext.resolution, envCaptureContext.resolution, true, TransientProperty(currentRetentionPolicy))) ?: return null



        val projection = Projection(2f * envCaptureContext.near, 2f * envCaptureContext.near, envCaptureContext.near, envCaptureContext.far, FrustumProjectionMode)
        val localRenderContext = RenderContext(envCaptureContext.resolution, envCaptureContext.resolution)
        localRenderContext.projection = projection
        mapOf(
            GL_TEXTURE_CUBE_MAP_NEGATIVE_X to DefaultCamera(envCaptureContext.position, -1.x, -1.y),
            GL_TEXTURE_CUBE_MAP_NEGATIVE_Y to DefaultCamera(envCaptureContext.position, -1.y, -1.z),
            GL_TEXTURE_CUBE_MAP_NEGATIVE_Z to DefaultCamera(envCaptureContext.position, -1.z, -1.y),
            GL_TEXTURE_CUBE_MAP_POSITIVE_X to DefaultCamera(envCaptureContext.position, 1.x, -1.y),
            GL_TEXTURE_CUBE_MAP_POSITIVE_Y to DefaultCamera(envCaptureContext.position, 1.y, 1.z),
            GL_TEXTURE_CUBE_MAP_POSITIVE_Z to DefaultCamera(envCaptureContext.position, 1.z, -1.y),
        ).forEach {
            val frameContext = CustomFrameContext(
                projection,
                it.value,
                renderContext,
                envCaptureContext.resolution
            )
            localRenderContext.camera = it.value
            inventory.uniformBufferHolder.populateFrame(
                listOf(
                    FrameMaterialModifier(frameContext),
                    LightMaterialModifier(envCaptureContext.sceneDeclaration)
                ), true
            )
            val contextMaterialModifier = ContextMaterialModifier(localRenderContext)
            probeFb.exec(it.key) {
                // TODO: pass correct contextMaterialModifier !!!!
                prepareScene(envCaptureContext.resolution, envCaptureContext.resolution, envCaptureContext.insideOut)
                success = success and renderForwardOpaques(envCaptureContext.sceneDeclaration)
                success = success and renderTransparents(envCaptureContext.sceneDeclaration, it.value, envCaptureContext.insideOut)
            }
        }
        probeFb.finish()
        return if (success) probeFb.colorTexture else null
    }

    fun renderToFrameProbe(frameCaptureContext: FrameCaptureContext, frameProbeName: String): GlGpuTexture? {

        val localRenderContext = RenderContext(frameCaptureContext.width, frameCaptureContext.height)
        localRenderContext.projection = frameCaptureContext.projection
        localRenderContext.camera = frameCaptureContext.camera

        inventory.uniformBufferHolder.populateFrame(
            listOf(
                renderContext.frameMaterialModifier,
                LightMaterialModifier(frameCaptureContext.sceneDeclaration)
            ), true
        )

        val contextMaterialModifier = ContextMaterialModifier(localRenderContext)
        val probeFb = inventory.frameBuffer(FrameBufferDeclaration("probe-$frameProbeName", frameCaptureContext.width, frameCaptureContext.height, listOf(GlGpuTexture.Preset.RGBAFilter), true, TransientProperty(currentRetentionPolicy)))
            ?: return null

        var success = true
        probeFb.exec {
            // TODO: use correct contextMaterialModifier!
            prepareScene(frameCaptureContext.width, frameCaptureContext.height)
            success = success and renderForwardOpaques(sceneDeclaration)
            success = success and renderTransparents(
                sceneDeclaration, frameCaptureContext.camera,
                width = frameCaptureContext.width, height = frameCaptureContext.height
            )
        }
        return if (success) probeFb.colorTextures[0] else null
    }

    fun renderRenderable(
        declaration: RenderableDeclaration,
        camera: Camera?,
        reverseZ: Boolean = false,
        isShadow: Boolean = false,
    ): Boolean {
        val meshLink = inventory.mesh(declaration.mesh as InternalMeshDeclaration) ?: return false
        val instancingMaterialModifier = (declaration.mesh as? Instanceable)?.instancing(meshLink, reverseZ, camera, inventory)
        val materialModifiers =  listOfNotNull(
            contextMaterialModifier,
            instancingMaterialModifier,
            ModelModifier(declaration.transform.mat4)
        ) + declaration.modifiers
        val materialDeclaration = declaration.material.toDeclaration(deferredShading, declaration.retentionPolicy, materialModifiers)
        if (materialDeclaration.defs.contains("NO_SHADOW_CAST") && isShadow)
            return true
        val shader = inventory.shader(materialDeclaration) ?: return false
        shader.render(materialDeclaration.uniformSuppliers, fixer, meshLink.gpuMesh)
        return true
    }
}

private class ModelModifier(
    val model: Mat4,
) : InternalMaterialModifier("model" to Mat4Getter<ModelModifier> { it.model })


internal class SkipRender(val text: String) : RuntimeException()

