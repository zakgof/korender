package com.zakgof.korender.impl.engine

import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.impl.camera.Camera
import com.zakgof.korender.impl.camera.DefaultCamera
import com.zakgof.korender.impl.engine.shadow.ShadowRenderer
import com.zakgof.korender.impl.engine.shadow.ShadowerData
import com.zakgof.korender.impl.engine.shadow.uniforms
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
import com.zakgof.korender.impl.glgpu.Color3List
import com.zakgof.korender.impl.glgpu.GlGpuCubeTexture
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.glgpu.IntList
import com.zakgof.korender.impl.glgpu.Vec3List
import com.zakgof.korender.impl.gltf.GltfSceneBuilder
import com.zakgof.korender.impl.material.ImageCubeTextureDeclaration
import com.zakgof.korender.impl.material.ImageTexture3DDeclaration
import com.zakgof.korender.impl.material.InternalMaterialModifier
import com.zakgof.korender.impl.material.InternalPostShadingEffect
import com.zakgof.korender.impl.material.InternalTexture
import com.zakgof.korender.impl.material.NotYetLoadedTexture
import com.zakgof.korender.impl.material.ProbeCubeTextureDeclaration
import com.zakgof.korender.impl.material.ProbeTextureDeclaration
import com.zakgof.korender.impl.material.ResourceCubeTextureDeclaration
import com.zakgof.korender.impl.material.materialDeclaration
import com.zakgof.korender.impl.projection.FrustumProjectionMode
import com.zakgof.korender.impl.projection.Projection
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

internal class Scene(
    private val sceneDeclaration: SceneDeclaration,
    val inventory: Inventory,
    val renderContext: RenderContext,
    val currentRetentionPolicy: RetentionPolicy
) {

    private val deferredShading = sceneDeclaration.deferredShadingDeclaration != null

    private val contextAdditionalUniforms = mutableMapOf<String, Any?>()

    private val contextMaterialModifier = InternalMaterialModifier {
        renderContext.contextUniforms(it.uniforms)
        it.plugins += renderContext.contextPlugins()
        it.uniforms += contextAdditionalUniforms
    }

    val touchBoxes = mutableListOf<TouchBox>()

    // TODO (backlog): ugly
    private val fixer = { value: Any? ->
        when (value) {
            is InternalTexture -> inventory.texture(value) ?: NotYetLoadedTexture
            is ProbeTextureDeclaration -> renderContext.frameProbes[value.frameProbeName] ?: NotYetLoadedTexture
            is ResourceCubeTextureDeclaration -> inventory.cubeTexture(value) ?: NotYetLoadedTexture
            is ImageCubeTextureDeclaration -> inventory.cubeTexture(value) ?: NotYetLoadedTexture
            is ProbeCubeTextureDeclaration -> renderContext.envProbes[value.envProbeName] ?: NotYetLoadedTexture
            is ImageTexture3DDeclaration -> inventory.texture3D(value) ?: NotYetLoadedTexture
            else -> value
        }
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

        val frameUniforms = mutableMapOf<String, Any?>()
        renderContext.frameUniforms(frameUniforms)
        fillLightUniforms(frameUniforms)

        try {
            renderShadows(frameUniforms, contextAdditionalUniforms)

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

        val postShadingEffects = sceneDeclaration.deferredShadingDeclaration!!.postShadingEffects
        var prevFb = renderDeferredShading(postShadingEffects.isNotEmpty() || sceneDeclaration.filters.isNotEmpty())

        val geometryDepth = contextAdditionalUniforms["depthTexture"]
        if (postShadingEffects.isNotEmpty()) {
            postShadingEffects.forEach {
                prevFb = renderPostShadingEffect(it as InternalPostShadingEffect, prevFb)
            }
            contextAdditionalUniforms["depthTexture"] = geometryDepth
            prevFb = renderComposition(sceneDeclaration.filters.isNotEmpty(), prevFb)
        }
        renderPostProcess(prevFb)
        // TODO fog over transparents ??
        renderTransparents(sceneDeclaration, renderContext.camera)
    }

    private fun renderDeferredShading(reusable: Boolean): ReusableFrameBufferDefinition? {
        val modifiers = listOf(contextMaterialModifier) + sceneDeclaration.deferredShadingDeclaration!!.shadingModifiers
        val shadingMaterial = materialDeclaration(BaseMaterial.Shading, true, currentRetentionPolicy, modifiers)
        val target = if (reusable) renderContext.defaultTarget() else null
        return renderToReusableFb(target, null) {
            renderFullscreen(shadingMaterial)
            renderBucket(sceneDeclaration.skies)
        }
    }

    private fun renderPostShadingEffect(effect: InternalPostShadingEffect, prevFb: ReusableFrameBufferDefinition?): ReusableFrameBufferDefinition? {
        var prevFb1 = prevFb
        effect.effectPasses.forEach { pass ->
            prevFb1 = renderToReusableFb(pass.target, prevFb1) {
                contextAdditionalUniforms["colorInputTexture"] = contextAdditionalUniforms[pass.colorInput]
                contextAdditionalUniforms["depthInputTexture"] = contextAdditionalUniforms[pass.depthInput]
                val material = materialDeclaration(BaseMaterial.Screen, true, currentRetentionPolicy, listOf(contextMaterialModifier) + pass.modifiers)
                renderFullscreen(material, pass.target.width, pass.target.height)
            }
        }
        return prevFb
    }

    private fun renderComposition(reusable: Boolean, prevFb: ReusableFrameBufferDefinition?): ReusableFrameBufferDefinition? {
        val compositionModifiers = listOf(contextMaterialModifier) + sceneDeclaration.deferredShadingDeclaration!!.postShadingEffects
            .map { it as InternalPostShadingEffect }
            .map { it.compositionMaterialModifier }
        val shadingMaterial = materialDeclaration(BaseMaterial.Composition, true, currentRetentionPolicy, compositionModifiers)
        val target = if (reusable) renderContext.defaultTarget() else null
        return renderToReusableFb(target, prevFb) {
            renderFullscreen(shadingMaterial)
        }
    }

    private fun renderSceneForward() {
        val target = if (sceneDeclaration.filters.isNotEmpty()) renderContext.defaultTarget() else null
        val prevFb = renderToReusableFb(target, null) {
            prepareScene()
            renderForwardOpaques(sceneDeclaration)
        }
        renderPostProcess(prevFb)
        // TODO: fog over transparents !!!
        renderTransparents(sceneDeclaration, renderContext.camera)
    }

    private fun renderPostProcess(prevFb: ReusableFrameBufferDefinition?) {
        val passes = sceneDeclaration.filters.flatMap { it.passes }
        if (passes.isNotEmpty()) {
            var prevFb1 = prevFb
            passes.dropLast(1).forEach { pass ->
                prevFb1 = renderToReusableFb(pass.target, prevFb1) {
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
                    GlGpuTexture.Preset.RGBANoFilter, // TODO review
                    GlGpuTexture.Preset.RGBANoFilter,
                    GlGpuTexture.Preset.RGBANoFilter,
                    GlGpuTexture.Preset.RGBANoFilter,
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
        contextAdditionalUniforms["diffuseGeometryTexture"] = geometryBuffer.colorTextures[0]
        contextAdditionalUniforms["normalGeometryTexture"] = geometryBuffer.colorTextures[1]
        contextAdditionalUniforms["materialGeometryTexture"] = geometryBuffer.colorTextures[2]
        contextAdditionalUniforms["emissionGeometryTexture"] = geometryBuffer.colorTextures[3]
        contextAdditionalUniforms["depthGeometryTexture"] = geometryBuffer.depthTexture!!

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
                    listOf(GlGpuTexture.Preset.RGBFilter, GlGpuTexture.Preset.RGBFilter, GlGpuTexture.Preset.RGBAFilter), false, TransientProperty(currentRetentionPolicy)
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

                        val materialModifiers = decalDeclaration.materialModifiers + InternalMaterialModifier {
                            it.uniforms["renderSize"] = Vec2(renderContext.width.toFloat(), renderContext.height.toFloat())
                        }
                        val renderableDeclaration = RenderableDeclaration(BaseMaterial.Decal, materialModifiers, DecalCube(0.5f, currentRetentionPolicy), Transform(model), true, currentRetentionPolicy)
                        renderRenderable(renderableDeclaration, renderContext.camera)
                    }
                    inventory.uniformBufferHolder.flush()
                }
                contextAdditionalUniforms["decalDiffuse"] = decalsFb.colorTextures[0]
                contextAdditionalUniforms["decalNormal"] = decalsFb.colorTextures[1]
                contextAdditionalUniforms["decalMaterial"] = decalsFb.colorTextures[2]
                decalBlendFb.exec {

                    val blendMaterialDeclaration = materialDeclaration(
                        BaseMaterial.DecalBlend,
                        true,
                        currentRetentionPolicy,
                        listOf(contextMaterialModifier)
                    )
                    renderFullscreen(blendMaterialDeclaration) { blend(false) }
                }
                contextAdditionalUniforms["diffuseGeometryTexture"] = decalBlendFb.colorTextures[0]
                contextAdditionalUniforms["normalGeometryTexture"] = decalBlendFb.colorTextures[1]
                contextAdditionalUniforms["materialGeometryTexture"] = decalBlendFb.colorTextures[2]
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
        insideOut: Boolean = false
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
        height: Int = renderContext.height
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

    private fun fillLightUniforms(m: MutableMap<String, Any?>) {
        m["numDirectionalLights"] = sceneDeclaration.directionalLights.size
        m["directionalLightDir[0]"] = Vec3List(sceneDeclaration.directionalLights.map { it.direction })
        m["directionalLightColor[0]"] = Color3List(sceneDeclaration.directionalLights.map { it.color })
        m["directionalLightShadowTextureIndex[0]"] = IntList(List(32) { -1 })
        m["directionalLightShadowTextureCount[0]"] = IntList(List(32) { 0 })
        m["ambientColor"] = sceneDeclaration.ambientLightColor
        m["numPointLights"] = sceneDeclaration.pointLights.size
        m["pointLightPos[0]"] = Vec3List(sceneDeclaration.pointLights.map { it.position })
        m["pointLightColor[0]"] = Color3List(sceneDeclaration.pointLights.map { it.color })
        m["pointLightAttenuation[0]"] = Vec3List(sceneDeclaration.pointLights.map { it.attenuation })
        inventory.uniformBufferHolder.populateFrame({ m[it] }, true)
    }

    private fun renderShadows(m: MutableMap<String, Any?>, u: MutableMap<String, Any?>) {
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
        shadowData.uniforms(m, u)
        m["directionalLightShadowTextureIndex[0]"] = IntList(directionalShadowIndexes)
        m["directionalLightShadowTextureCount[0]"] = IntList(directionalShadowCounts)
        inventory.uniformBufferHolder.populateFrame({ m[it] })
    }

    private fun renderPostProcessPass(pass: InternalPassDeclaration) {
        contextAdditionalUniforms["colorInputTexture"] = contextAdditionalUniforms[pass.colorInput]
        contextAdditionalUniforms["depthInputTexture"] = contextAdditionalUniforms[pass.depthInput]
        val filterMaterial = materialDeclaration(BaseMaterial.Screen, deferredShading, pass.retentionPolicy, listOf(contextMaterialModifier) + pass.modifiers)
        renderFullscreen(filterMaterial, pass.target.width, pass.target.height)
        pass.sceneDeclaration?.let { renderForwardOpaques(it) }
    }

    private fun renderFullscreen(
        quadMaterial: MaterialDeclaration,
        width: Int = renderContext.width,
        height: Int = renderContext.height,
        state: GlState.StateContext.() -> Unit = {}
    ) {
        val mesh = inventory.mesh(ScreenQuad(currentRetentionPolicy))
        val shader = inventory.shader(quadMaterial.shader)
        renderContext.state.set {
            state()
        }
        glViewport(0, 0, width, height)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        if (mesh != null && shader != null) {
            shader.render(
                { fixer(quadMaterial.uniforms[it]) },
                mesh.gpuMesh
            )
            inventory.uniformBufferHolder.flush()
        }
    }

    private fun renderToReusableFb(target: FrameTarget?, prevFb: ReusableFrameBufferDefinition?, block: () -> Unit): ReusableFrameBufferDefinition? {
        if (target == null) {
            block()
            return null
        } else {
            val pingPong = if (prevFb != null && prevFb.width == target.width && prevFb.height == target.height) 1 - prevFb.pingPong else 0
            val fb = inventory.frameBuffer(FrameBufferDeclaration("filter-$pingPong", target!!.width, target.height, listOf(GlGpuTexture.Preset.RGBFilter), true, TransientProperty(currentRetentionPolicy)))
                ?: throw SkipRender("Reusable FB 'filter-$pingPong'")
            fb.exec { block() }
            contextAdditionalUniforms[target.colorOutput] = fb.colorTextures[0]
            contextAdditionalUniforms[target.depthOutput] = fb.depthTexture
            return ReusableFrameBufferDefinition(pingPong, target.width, target.height)
        }
    }

    fun renderToEnvProbe(envCaptureContext: EnvCaptureContext, probeName: String): GlGpuCubeTexture? {
        var success = true
        val probeFb =
            inventory.cubeFrameBuffer(CubeFrameBufferDeclaration("probe-$probeName", envCaptureContext.resolution, envCaptureContext.resolution, true, TransientProperty(currentRetentionPolicy))) ?: return null
        val projection = Projection(2f * envCaptureContext.near, 2f * envCaptureContext.near, envCaptureContext.near, envCaptureContext.far, FrustumProjectionMode)
        mapOf(
            GL_TEXTURE_CUBE_MAP_NEGATIVE_X to DefaultCamera(envCaptureContext.position, -1.x, -1.y),
            GL_TEXTURE_CUBE_MAP_NEGATIVE_Y to DefaultCamera(envCaptureContext.position, -1.y, -1.z),
            GL_TEXTURE_CUBE_MAP_NEGATIVE_Z to DefaultCamera(envCaptureContext.position, -1.z, -1.y),
            GL_TEXTURE_CUBE_MAP_POSITIVE_X to DefaultCamera(envCaptureContext.position, 1.x, -1.y),
            GL_TEXTURE_CUBE_MAP_POSITIVE_Y to DefaultCamera(envCaptureContext.position, 1.y, 1.z),
            GL_TEXTURE_CUBE_MAP_POSITIVE_Z to DefaultCamera(envCaptureContext.position, 1.z, -1.y),
        ).forEach {
            val frameUniforms = mutableMapOf<String, Any?>()
            renderContext.frameUniforms(frameUniforms)
            frameUniforms["view"] = it.value.mat4
            frameUniforms["projectionWidth"] = projection.width
            frameUniforms["projectionHeight"] = projection.height
            frameUniforms["projectionNear"] = projection.near
            frameUniforms["projectionFar"] = projection.far
            frameUniforms["cameraPos"] = it.value.position
            frameUniforms["cameraDir"] = it.value.direction
            fillLightUniforms(frameUniforms)

            val contextUniforms = mutableMapOf<String, Any?>()
            renderContext.contextUniforms(contextUniforms)

            probeFb.exec(it.key) {
                prepareScene(envCaptureContext.resolution, envCaptureContext.resolution, envCaptureContext.insideOut)
                success = success and renderForwardOpaques(sceneDeclaration)
                success = success and renderTransparents(sceneDeclaration, it.value, envCaptureContext.insideOut)
            }
        }
        probeFb.finish()
        return if (success) probeFb.colorTexture else null
    }

    fun renderToFrameProbe(frameCaptureContext: FrameCaptureContext, frameProbeName: String): GlGpuTexture? {
        val frameUniforms = mutableMapOf<String, Any?>()
        renderContext.frameUniforms(frameUniforms)
        frameUniforms["view"] = frameCaptureContext.camera.mat4
        frameUniforms["projectionWidth"] = frameCaptureContext.projection.width
        frameUniforms["projectionHeight"] = frameCaptureContext.projection.height
        frameUniforms["projectionNear"] = frameCaptureContext.projection.near
        frameUniforms["projectionFar"] = frameCaptureContext.projection.far
        frameUniforms["cameraPos"] = frameCaptureContext.camera.position
        frameUniforms["cameraDir"] = frameCaptureContext.camera.direction
        fillLightUniforms(frameUniforms)

        val probeFb = inventory.frameBuffer(FrameBufferDeclaration("probe-$frameProbeName", frameCaptureContext.width, frameCaptureContext.height, listOf(GlGpuTexture.Preset.RGBAFilter), true, TransientProperty(currentRetentionPolicy)))
            ?: return null

        var success = true
        probeFb.exec {
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
        isShadow: Boolean = false
    ): Boolean {
        val addUniforms = mutableMapOf<String, Any?>()
        val addDefs = mutableSetOf<String>()

        val meshLink = inventory.mesh(declaration.mesh as InternalMeshDeclaration) ?: return false

        if (declaration.mesh is Instanceable) {
            declaration.mesh.instancing(meshLink, reverseZ, camera, inventory, addUniforms, addDefs)
        }

        val materialModifiers = listOf(contextMaterialModifier) + declaration.materialModifiers + InternalMaterialModifier { it.shaderDefs += addDefs }
        val materialDeclaration = materialDeclaration(declaration.base, deferredShading, declaration.retentionPolicy, materialModifiers)

        if (materialDeclaration.shader.defs.contains("NO_SHADOW_CAST") && isShadow)
            return true

        val shader = inventory.shader(materialDeclaration.shader) ?: return false

        // TODO move this to where it is supported
        addUniforms["model"] = declaration.transform.mat4
        shader.render(
            { fixer(materialDeclaration.uniforms[it] ?: addUniforms[it]) },
            meshLink.gpuMesh
        )
        return true
    }
}

internal class SkipRender(val text: String) : RuntimeException()

