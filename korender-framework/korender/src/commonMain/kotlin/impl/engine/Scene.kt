package com.zakgof.korender.impl.engine

import GltfSceneBuilder
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.impl.camera.Camera
import com.zakgof.korender.impl.camera.DefaultCamera
import com.zakgof.korender.impl.engine.shadow.ShadowRenderer
import com.zakgof.korender.impl.engine.shadow.ShadowerData
import com.zakgof.korender.impl.engine.shadow.uniforms
import com.zakgof.korender.impl.geometry.DecalCube
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
import com.zakgof.korender.impl.glgpu.GlGpuFrameBuffer
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.glgpu.IntList
import com.zakgof.korender.impl.glgpu.Vec3List
import com.zakgof.korender.impl.material.ImageCubeTextureDeclaration
import com.zakgof.korender.impl.material.InternalPostShadingEffect
import com.zakgof.korender.impl.material.InternalTexture
import com.zakgof.korender.impl.material.NotYetLoadedTexture
import com.zakgof.korender.impl.material.ProbeCubeTextureDeclaration
import com.zakgof.korender.impl.material.ProbeTextureDeclaration
import com.zakgof.korender.impl.material.ResourceCubeTextureDeclaration
import com.zakgof.korender.impl.material.materialDeclaration
import com.zakgof.korender.impl.projection.FrustumProjection
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

internal class Scene(
    private val sceneDeclaration: SceneDeclaration,
    private val inventory: Inventory,
    private val renderContext: RenderContext,
    private val currentRetentionPolicy: RetentionPolicy
) {

    private val deferredShading = sceneDeclaration.deferredShadingDeclaration != null

    val touchBoxes = mutableListOf<TouchBox>()

    // TODO (backlog): ugly
    private val fixer = { value: Any? ->
        when (value) {
            is InternalTexture -> inventory.texture(value) ?: NotYetLoadedTexture
            is ProbeTextureDeclaration -> renderContext.frameProbes[value.frameProbeName] ?: NotYetLoadedTexture
            is ResourceCubeTextureDeclaration -> inventory.cubeTexture(value) ?: NotYetLoadedTexture
            is ImageCubeTextureDeclaration -> inventory.cubeTexture(value) ?: NotYetLoadedTexture
            is ProbeCubeTextureDeclaration -> renderContext.envProbes[value.envProbeName] ?: NotYetLoadedTexture
            else -> value
        }
    }

    init {
        sceneDeclaration.gltfs.forEach {
            inventory.gltf(it)?.let { gltfLoaded ->
                // TODO: support transparency
                sceneDeclaration.opaques += GltfSceneBuilder(it, gltfLoaded).build()
            }
        }
    }

    fun render(): Boolean {

        val uniforms = mutableMapOf<String, Any?>()
        renderContext.uniforms(uniforms)

        renderEnvProbes(uniforms)
        renderFrameProbes(uniforms)

        try {
            renderShadows(uniforms, false)
            if (deferredShading) {
                renderSceneDeferred(uniforms)
            } else {
                renderSceneForward(uniforms)
            }
            return true
        } catch (sr: SkipRender) {
            println("Scene rendering skipped as resource not ready: [${sr.text}]")
            return false
        }
    }

    private fun renderEnvProbes(uniforms: MutableMap<String, Any?>) {
        sceneDeclaration.envCaptures.forEach { kv ->
            try {
                renderContext.envProbes[kv.key] = Scene(kv.value.sceneDeclaration, inventory, renderContext, currentRetentionPolicy)
                    .renderToEnvProbe(uniforms, kv.value, kv.key)
            } catch (sr: SkipRender) {
                println("Env probing skipped as resource not ready: [${sr.text}]")
                return
            }
        }
    }

    private fun renderFrameProbes(uniforms: MutableMap<String, Any?>) {
        sceneDeclaration.frameCaptures.forEach { kv ->
            try {
                renderContext.frameProbes[kv.key] = Scene(kv.value.sceneDeclaration, inventory, renderContext, currentRetentionPolicy)
                    .renderToFrameProbe(uniforms, kv.value, kv.key)
            } catch (sr: SkipRender) {
                println("Frame probing skipped as resource not ready: [${sr.text}]")
                return
            }
        }
    }

    private fun renderSceneDeferred(uniforms: MutableMap<String, Any?>) {

        renderDeferredOpaques(uniforms)

        val postShadingEffects = sceneDeclaration.deferredShadingDeclaration!!.postShadingEffects
        renderDeferredShading(uniforms, if (postShadingEffects.isEmpty() && sceneDeclaration.filters.isEmpty()) null else sceneDeclaration.filters.size + 1)

        if (postShadingEffects.isNotEmpty()) {
            postShadingEffects.forEach { renderPostShadingEffect(it as InternalPostShadingEffect, uniforms) }
            renderComposition(uniforms, if (sceneDeclaration.filters.isEmpty()) null else sceneDeclaration.filters.size)
        }
        sceneDeclaration.filters.forEachIndexed { filterIndex, filter ->
            renderToReusableFb(if (sceneDeclaration.filters.size - 1 == filterIndex) null else sceneDeclaration.filters.size - 1 - filterIndex, uniforms) {
                renderPostProcess(filter, uniforms)
            }
        }
        // TODO fog over transparents ??
        renderTransparents(sceneDeclaration, uniforms, renderContext.camera)
    }

    private fun renderDeferredShading(uniforms: MutableMap<String, Any?>, fbIndex: Int?) {
        renderToReusableFb(fbIndex, uniforms) {
            val shadingMaterial = materialDeclaration(BaseMaterial.Shading, true, currentRetentionPolicy, sceneDeclaration.deferredShadingDeclaration!!.shadingModifiers)
            renderFullscreen(shadingMaterial, uniforms)
            renderBucket(sceneDeclaration.skies, uniforms)
        }?.let {
            uniforms["finalColorTexture"] = it.colorTextures[0]
            uniforms["depthTexture"] = it.depthTexture
        }
    }

    private fun renderPostShadingEffect(effect: InternalPostShadingEffect, uniforms: MutableMap<String, Any?>) {
        effect.effectPassMaterialModifiers.forEachIndexed { passIndex, effectMM ->

            val fbName = if (passIndex == effect.effectPassMaterialModifiers.size - 1)
                "effect-${effect.name}"
            else
                "effect-${(effect.effectPassMaterialModifiers.size - 1 - passIndex) % 2}"

            val fb = inventory.frameBuffer(
                FrameBufferDeclaration(fbName, effect.width, effect.height, listOf(GlGpuTexture.Preset.RGBFilter), true, TransientProperty(effect.retentionPolicy))
            ) ?: throw SkipRender("Post-shading effect FB $fbName")
            val material = materialDeclaration(BaseMaterial.Screen, true, currentRetentionPolicy, listOf(effectMM))
            fb.exec {
                renderFullscreen(material, uniforms, effect.width, effect.height)
            }
            if (passIndex == effect.effectPassMaterialModifiers.size - 1) {
                uniforms[effect.compositionColorOutput] = fb.colorTextures[0]
                uniforms[effect.compositionDepthOutput] = fb.depthTexture
            } else {
                uniforms["colorTexture"] = fb.colorTextures[0]
                uniforms["depthTexture"] = fb.depthTexture
            }
        }
    }

    private fun renderComposition(uniforms: MutableMap<String, Any?>, fbIndex: Int?) {
        val compositionModifiers = sceneDeclaration.deferredShadingDeclaration!!.postShadingEffects
            .map { it as InternalPostShadingEffect }
            .map { it.compositionMaterialModifier }

        renderToReusableFb(fbIndex, uniforms) {
            val shadingMaterial = materialDeclaration(BaseMaterial.Composition, true, currentRetentionPolicy, compositionModifiers)
            renderFullscreen(shadingMaterial, uniforms)
        }
    }

    private fun renderSceneForward(uniforms: MutableMap<String, Any?>) {
        if (sceneDeclaration.filters.isEmpty()) {
            prepareScene()
            renderForwardOpaques(sceneDeclaration, uniforms)
            renderTransparents(sceneDeclaration, uniforms, renderContext.camera)
        } else {
            renderToReusableFb(0, uniforms) {
                prepareScene()
                renderForwardOpaques(sceneDeclaration, uniforms)
            }
            sceneDeclaration.filters.dropLast(1).forEachIndexed { index, filter ->
                renderToReusableFb(index + 1, uniforms) {
                    renderPostProcess(filter, uniforms)
                }
            }
            renderPostProcess(sceneDeclaration.filters.last(), uniforms)
            // TODO: fog over transparents !!!
            renderTransparents(sceneDeclaration, uniforms, renderContext.camera)
        }
    }

    private fun renderDeferredOpaques(uniforms: MutableMap<String, Any?>) {
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
            renderBucket(sceneDeclaration.opaques, uniforms)
        }
        uniforms["cdiffTexture"] = geometryBuffer.colorTextures[0]
        uniforms["normalTexture"] = geometryBuffer.colorTextures[1]
        uniforms["materialTexture"] = geometryBuffer.colorTextures[2]
        uniforms["emissionTexture"] = geometryBuffer.colorTextures[3]
        uniforms["depthTexture"] = geometryBuffer.depthTexture!!

        renderDecals(uniforms)
    }

    private fun renderDecals(uniforms: MutableMap<String, Any?>) {
        if (sceneDeclaration.deferredShadingDeclaration!!.decals.isNotEmpty()) {
            // TODO: downscaled
            val decalsFb = inventory.frameBuffer(
                FrameBufferDeclaration(
                    "decals", renderContext.width, renderContext.height,
                    listOf(GlGpuTexture.Preset.RGBAFilter, GlGpuTexture.Preset.RGBAFilter), true, TransientProperty(currentRetentionPolicy)
                )
            )
            val meshLink = inventory.mesh(DecalCube(0.5f, currentRetentionPolicy))
            val shader = inventory.shader(ShaderDeclaration("!shader/deferred/decal.vert", "!shader/deferred/decal.frag", retentionPolicy = currentRetentionPolicy))

            if (meshLink != null && shader != null && decalsFb != null) {
                decalsFb.exec {

                    renderContext.state.set {}
                    glViewport(0, 0, renderContext.width, renderContext.height)
                    glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

                    sceneDeclaration.deferredShadingDeclaration!!.decals.forEach { decalDeclaration ->

                        val right = decalDeclaration.look % decalDeclaration.up
                        val model = Mat4(
                            right.x, right.y, right.z, decalDeclaration.position.x,
                            decalDeclaration.up.x, decalDeclaration.up.y, decalDeclaration.up.z, decalDeclaration.position.y,
                            -decalDeclaration.look.x, -decalDeclaration.look.y, -decalDeclaration.look.z, decalDeclaration.position.z,
                            0f, 0f, 0f, 1f
                        )



                        val decalUniforms = mapOf(
                            "renderSize" to Vec2(renderContext.width.toFloat(), renderContext.height.toFloat()),
                            "decalTexture" to decalDeclaration.colorTexture,
                            "model" to model * scale(decalDeclaration.size).mat4
                        )

                        println((decalUniforms["model"] as Mat4).project(Vec3.ZERO))


                        shader.render(
                            { fixer(decalUniforms[it] ?: uniforms[it]) },
                            meshLink.gpuMesh
                        )
                    }
                    uniforms["cdiffTexture"] = decalsFb.colorTextures[0]
                }
            }
        }
    }

    private fun renderBucket(renderables: List<RenderableDeclaration>, uniforms: Map<String, Any?>, defs: Set<String> = setOf()) {
        renderables.forEach {
            Rendering.render(inventory, it, renderContext.camera, deferredShading, uniforms, fixer, defs)
        }
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

    private fun renderForwardOpaques(
        sceneDeclaration: SceneDeclaration,
        uniforms: Map<String, Any?>,
        defs: Set<String> = setOf(),
    ) {
        renderBucket(sceneDeclaration.opaques, uniforms, defs)
        renderBucket(sceneDeclaration.skies, uniforms, defs)
    }

    private fun renderTransparents(
        sceneDeclaration: SceneDeclaration,
        uniforms: MutableMap<String, Any?>,
        camera: Camera,
        defs: Set<String> = setOf(),
        insideOut: Boolean = false,
        width: Int = renderContext.width,
        height: Int = renderContext.height
    ) {
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
                Rendering.render(
                    inventory, it, camera, deferredShading,
                    uniforms, fixer, defs, insideOut
                )
            }

        val guiRenderers = sceneDeclaration.guis.map {
            GuiRenderer(inventory, width, height, it)
        }
        touchBoxes += guiRenderers.flatMap { it.touchBoxes }
        guiRenderers.flatMap { it.renderables }.forEach {
            it.render(uniforms, fixer)
        }
    }

    private fun renderShadows(m: MutableMap<String, Any?>, forceNoShadows: Boolean) {
        val shadowData = mutableListOf<ShadowerData>()
        val directionalDirs = mutableListOf<Vec3>()
        val directionalColors = mutableListOf<ColorRGB>()
        val directionalShadowIndexes = mutableListOf<Int>()
        val directionalShadowCounts = mutableListOf<Int>()
        sceneDeclaration.directionalLights.forEachIndexed { li, dl ->
            val indexes = if (forceNoShadows)
                listOf()
            else
                dl.shadowDeclaration.cascades.mapIndexedNotNull { ci, cascadeDeclaration ->
                    ShadowRenderer.render(
                        "$li-$ci",
                        inventory,
                        dl.direction,
                        dl.shadowDeclaration.cascades,
                        ci,
                        renderContext,
                        sceneDeclaration.opaques + sceneDeclaration.transparents,
                        fixer
                    )?.let {
                        shadowData += it
                        shadowData.size - 1
                    }
                }
            directionalDirs += dl.direction
            directionalColors += dl.color
            directionalShadowIndexes += indexes.minOrNull() ?: -1
            directionalShadowCounts += indexes.size
        }

        shadowData.uniforms(m)

        m["numDirectionalLights"] = sceneDeclaration.directionalLights.size
        m["directionalLightDir[0]"] = Vec3List(directionalDirs)
        m["directionalLightColor[0]"] = Color3List(directionalColors)
        m["directionalLightShadowTextureIndex[0]"] = IntList(directionalShadowIndexes)
        m["directionalLightShadowTextureCount[0]"] = IntList(directionalShadowCounts)
        m["ambientColor"] = sceneDeclaration.ambientLightColor
        m["numPointLights"] = sceneDeclaration.pointLights.size
        m["pointLightPos[0]"] = Vec3List(sceneDeclaration.pointLights.map { it.position })
        m["pointLightColor[0]"] = Color3List(sceneDeclaration.pointLights.map { it.color })
        m["pointLightAttenuation[0]"] = Vec3List(sceneDeclaration.pointLights.map { it.attenuation })
    }

    private fun renderPostProcess(filterDeclaration: InternalFilterDeclaration, uniforms: Map<String, Any?>) {
        val filterMaterial = materialDeclaration(BaseMaterial.Screen, deferredShading, filterDeclaration.retentionPolicy, filterDeclaration.modifiers)
        renderFullscreen(filterMaterial, uniforms)
        renderForwardOpaques(filterDeclaration.sceneDeclaration, uniforms)
    }

    private fun renderFullscreen(
        filter: MaterialDeclaration, uniforms: Map<String, Any?>,
        width: Int = renderContext.width, height: Int = renderContext.height
    ) {
        val mesh = inventory.mesh(ScreenQuad(currentRetentionPolicy))
        val shader = inventory.shader(filter.shader)
        renderContext.state.set {}
        glViewport(0, 0, width, height)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        if (mesh != null && shader != null) {
            shader.render(
                { fixer(filter.uniforms[it] ?: uniforms[it]) },
                mesh.gpuMesh
            )
        }
    }

    private fun renderToReusableFb(index: Int?, m: MutableMap<String, Any?>, block: () -> Unit): GlGpuFrameBuffer? {
        if (index == null) {
            block()
            return null
        } else {
            val number = index % 2
            val fb = inventory.frameBuffer(FrameBufferDeclaration("filter-$number", renderContext.width, renderContext.height, listOf(GlGpuTexture.Preset.RGBNoFilter), true, TransientProperty(currentRetentionPolicy)))
                ?: throw SkipRender("Reusable FB 'filter-$number'")
            fb.exec { block() }
            m["colorTexture"] = fb.colorTextures[0]
            m["depthTexture"] = fb.depthTexture
            return fb
        }
    }

    fun renderToEnvProbe(uniforms: MutableMap<String, Any?>, envCaptureContext: EnvCaptureContext, probeName: String): GlGpuCubeTexture {
        renderShadows(uniforms, true)
        val probeFb =
            inventory.cubeFrameBuffer(CubeFrameBufferDeclaration("probe-$probeName", envCaptureContext.resolution, envCaptureContext.resolution, true, TransientProperty(currentRetentionPolicy))) ?: throw SkipRender("Env probe FB 'probe-$probeName'")
        val probeUniforms = mutableMapOf<String, Any?>()
        probeUniforms += uniforms
        val projection = FrustumProjection(2f * envCaptureContext.near, 2f * envCaptureContext.near, envCaptureContext.near, envCaptureContext.far)
        mapOf(
            GL_TEXTURE_CUBE_MAP_NEGATIVE_X to DefaultCamera(envCaptureContext.position, -1.x, -1.y),
            GL_TEXTURE_CUBE_MAP_NEGATIVE_Y to DefaultCamera(envCaptureContext.position, -1.y, -1.z),
            GL_TEXTURE_CUBE_MAP_NEGATIVE_Z to DefaultCamera(envCaptureContext.position, -1.z, -1.y),
            GL_TEXTURE_CUBE_MAP_POSITIVE_X to DefaultCamera(envCaptureContext.position, 1.x, -1.y),
            GL_TEXTURE_CUBE_MAP_POSITIVE_Y to DefaultCamera(envCaptureContext.position, 1.y, 1.z),
            GL_TEXTURE_CUBE_MAP_POSITIVE_Z to DefaultCamera(envCaptureContext.position, 1.z, -1.y),
        ).forEach {
            probeUniforms["view"] = it.value.mat4
            probeUniforms["projection"] = projection.mat4
            probeUniforms["cameraPos"] = it.value.position
            probeUniforms["cameraDir"] = it.value.direction
            probeFb.exec(it.key) {
                prepareScene(envCaptureContext.resolution, envCaptureContext.resolution, envCaptureContext.insideOut)
                renderForwardOpaques(sceneDeclaration, probeUniforms, envCaptureContext.defs)
                renderTransparents(sceneDeclaration, probeUniforms, it.value, envCaptureContext.defs, envCaptureContext.insideOut)
            }
        }
        probeFb.finish()
        return probeFb.colorTexture;
    }

    fun renderToFrameProbe(uniforms: MutableMap<String, Any?>, frameCaptureContext: FrameCaptureContext, frameProbeName: String): GlGpuTexture {
        renderShadows(uniforms, true)
        val probeFb = inventory.frameBuffer(FrameBufferDeclaration("probe-$frameProbeName", frameCaptureContext.width, frameCaptureContext.height, listOf(GlGpuTexture.Preset.RGBAFilter), true, TransientProperty(currentRetentionPolicy)))
            ?: throw SkipRender("Frame probe FB 'probe-$frameProbeName'")
        val probeUniforms = mutableMapOf<String, Any?>()
        probeUniforms += uniforms
        probeUniforms["view"] = frameCaptureContext.camera.mat4
        probeUniforms["projection"] = frameCaptureContext.projection.mat4
        probeUniforms["cameraPos"] = frameCaptureContext.camera.position
        probeUniforms["cameraDir"] = frameCaptureContext.camera.direction
        probeFb.exec {
            prepareScene(frameCaptureContext.width, frameCaptureContext.height)
            renderForwardOpaques(sceneDeclaration, probeUniforms)
            renderTransparents(
                sceneDeclaration, probeUniforms, frameCaptureContext.camera,
                width = frameCaptureContext.width, height = frameCaptureContext.height
            )
        }
        return probeFb.colorTextures[0]
    }
}

internal class SkipRender(val text: String) : RuntimeException()

