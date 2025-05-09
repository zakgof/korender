package com.zakgof.korender.impl.engine

import com.zakgof.korender.impl.camera.Camera
import com.zakgof.korender.impl.camera.DefaultCamera
import com.zakgof.korender.impl.engine.shadow.ShadowRenderer
import com.zakgof.korender.impl.engine.shadow.ShadowerData
import com.zakgof.korender.impl.engine.shadow.uniforms
import com.zakgof.korender.impl.geometry.ScreenQuad
import com.zakgof.korender.impl.gl.GL.glClear
import com.zakgof.korender.impl.gl.GL.glClearColor
import com.zakgof.korender.impl.gl.GL.glDepthMask
import com.zakgof.korender.impl.gl.GL.glDisable
import com.zakgof.korender.impl.gl.GL.glEnable
import com.zakgof.korender.impl.gl.GL.glViewport
import com.zakgof.korender.impl.gl.GLConstants.GL_BLEND
import com.zakgof.korender.impl.gl.GLConstants.GL_COLOR_BUFFER_BIT
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_BUFFER_BIT
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP_NEGATIVE_X
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP_POSITIVE_X
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP_POSITIVE_Y
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP_POSITIVE_Z
import com.zakgof.korender.impl.glgpu.Color3List
import com.zakgof.korender.impl.glgpu.GlGpuFrameBuffer
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.glgpu.IntList
import com.zakgof.korender.impl.glgpu.Vec3List
import com.zakgof.korender.impl.gltf.GltfSceneBuilder
import com.zakgof.korender.impl.material.InternalPostShadingEffect
import com.zakgof.korender.impl.material.InternalTexture
import com.zakgof.korender.impl.material.NotYetLoadedCubeTexture
import com.zakgof.korender.impl.material.NotYetLoadedTexture
import com.zakgof.korender.impl.material.ResourceCubeTextureDeclaration
import com.zakgof.korender.impl.material.materialDeclaration
import com.zakgof.korender.impl.projection.FrustumProjection
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

internal class Scene(
    private val sceneDeclaration: SceneDeclaration,
    private val inventory: Inventory,
    private val renderContext: RenderContext,
    private val envSlot: Int? = null
) {

    private var guiRenderers: List<GuiRenderer>
    private val deferredShading = sceneDeclaration.deferredShadingDeclaration != null

    val touchBoxes: List<TouchBox>

    // TODO (backlog): ugly
    private val fixer = { value: Any? ->
        when (value) {
            is InternalTexture -> inventory.texture(value) ?: NotYetLoadedTexture
            is ResourceCubeTextureDeclaration -> inventory.cubeTexture(value) ?: NotYetLoadedCubeTexture
            else -> value
        }
    }
    private val filters = sceneDeclaration.filters.map { materialDeclaration(BaseMaterial.Screen, deferredShading, *it.toTypedArray()) }

    init {
        sceneDeclaration.gltfs.forEach {
            inventory.gltf(it)?.let { gltfLoaded ->
                sceneDeclaration.renderables += GltfSceneBuilder(it, gltfLoaded).build(it.time)
            }
        }
        guiRenderers = sceneDeclaration.guis.map {
            GuiRenderer(inventory, renderContext.width, renderContext.height, it)
        }
        touchBoxes = guiRenderers.flatMap { it.touchBoxes }
    }

    fun render() {

        val uniforms = mutableMapOf<String, Any?>()
        renderContext.uniforms(uniforms)

        sceneDeclaration.captures.forEach { kv ->
            try {
                Scene(kv.value.sceneDeclaration, inventory, renderContext, kv.key).renderToEnv(uniforms, kv.value)
            } catch (_: SkipRender) {
                println("Env probing skipped as framebuffer is not ready")
                return
            }
        }

        renderShadows(uniforms, false)

        try {
            if (deferredShading) {
                renderSceneDeferred(uniforms)
            } else {
                renderSceneForward(uniforms)
            }
        } catch (_: SkipRender) {
            println("Scene rendering skipped as framebuffers are not ready")
        }
    }

    private fun renderSceneDeferred(uniforms: MutableMap<String, Any?>) {

        renderDeferredOpaques(uniforms)

        val postShadingEffects = sceneDeclaration.deferredShadingDeclaration!!.postShadingEffects
        renderDeferredShading(uniforms, if (postShadingEffects.isEmpty() && filters.isEmpty()) null else filters.size + 1)

        if (postShadingEffects.isNotEmpty()) {
            postShadingEffects.forEach { renderPostShadingEffect(it as InternalPostShadingEffect, uniforms) }
            renderComposition(uniforms, if (filters.isEmpty()) null else filters.size)
        }
        filters.forEachIndexed { filterIndex, filter ->
            renderToReusableFb(if (filters.size - 1 == filterIndex) null else filters.size - 1 - filterIndex, uniforms) {
                renderFullscreen(filter, uniforms)
            }
        }
        renderTransparents(uniforms, renderContext.camera)
    }

    private fun renderDeferredShading(uniforms: MutableMap<String, Any?>, fbIndex: Int?) {
        renderToReusableFb(fbIndex, uniforms) {
            val shadingMaterial = materialDeclaration(BaseMaterial.Shading, true, *sceneDeclaration.deferredShadingDeclaration!!.shadingModifiers.toTypedArray())
            renderFullscreen(shadingMaterial, uniforms)
            renderBucket(uniforms, Bucket.SKY)
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
                FrameBufferDeclaration(fbName, effect.width, effect.height, listOf(GlGpuTexture.Preset.RGBFilter), true)
            ) ?: throw SkipRender
            val material = materialDeclaration(BaseMaterial.Screen, true, effectMM)
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
            .toTypedArray()

        renderToReusableFb(fbIndex, uniforms) {
            val shadingMaterial = materialDeclaration(BaseMaterial.Composition, true, *compositionModifiers)
            renderFullscreen(shadingMaterial, uniforms)
        }
    }

    private fun renderSceneForward(uniforms: MutableMap<String, Any?>) {
        if (sceneDeclaration.filters.isEmpty()) {
            renderForwardOpaques(uniforms, renderContext.width, renderContext.height)
            renderTransparents(uniforms, renderContext.camera)
        } else {
            renderToReusableFb(0, uniforms) {
                renderForwardOpaques(uniforms, renderContext.width, renderContext.height)
            }
            filters.dropLast(1).forEachIndexed { index, filter ->
                renderToReusableFb(index + 1, uniforms) {
                    renderFullscreen(filter, uniforms)
                }
            }
            renderFullscreen(filters.last(), uniforms)
            renderTransparents(uniforms, renderContext.camera)
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
                true
            )
        ) ?: throw SkipRender
        geometryBuffer.exec {
            glClearColor(0f, 0f, 0f, 1f)
            glViewport(0, 0, renderContext.width, renderContext.height)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            glDisable(GL_BLEND)
            renderBucket(uniforms, Bucket.OPAQUE)
            glEnable(GL_BLEND);
        }
        uniforms["cdiffTexture"] = geometryBuffer.colorTextures[0]
        uniforms["normalTexture"] = geometryBuffer.colorTextures[1]
        uniforms["materialTexture"] = geometryBuffer.colorTextures[2]
        uniforms["emissionTexture"] = geometryBuffer.colorTextures[3]
        uniforms["depthTexture"] = geometryBuffer.depthTexture!!
    }

    private fun renderBucket(uniforms: Map<String, Any?>, bucket: Bucket, vararg defs: String) {
        sceneDeclaration.renderables
            .filter { it.bucket == bucket }
            .forEach {
                Rendering.render(
                    inventory, it, renderContext.camera, deferredShading, uniforms, fixer, *defs
                )
            }
    }

    private fun renderForwardOpaques(uniforms: Map<String, Any?>, w: Int, h: Int, vararg defs: String) {
        val back = renderContext.backgroundColor
        glClearColor(back.r, back.g, back.b, 1.0f)
        glViewport(0, 0, w, h)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        renderBucket(uniforms, Bucket.OPAQUE, *defs)
        renderBucket(uniforms, Bucket.SKY, *defs)
    }

    private fun renderTransparents(uniforms: MutableMap<String, Any?>, camera: Camera) {
        glDepthMask(false)
        sceneDeclaration.renderables
            .filter { it.bucket == Bucket.TRANSPARENT }
            .sortedByDescending { (camera.mat4 * it.transform.offset()).z }
            .forEach {
                Rendering.render(
                    inventory, it, camera, deferredShading,
                    uniforms, fixer
                )
            }

        renderBucket(uniforms, Bucket.SCREEN)
        guiRenderers.flatMap { it.renderables }.forEach {
            it.render(uniforms, fixer)
        }

        glDepthMask(true)
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
                        sceneDeclaration.renderables,
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

    private fun renderFullscreen(
        filter: MaterialDeclaration, uniforms: Map<String, Any?>,
        width: Int = renderContext.width, height: Int = renderContext.height
    ) {
        val mesh = inventory.mesh(ScreenQuad)
        val shader = inventory.shader(filter.shader)
        glClearColor(0f, 0f, 0f, 1f)
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
            val fb = inventory.frameBuffer(FrameBufferDeclaration("filter-$number", renderContext.width, renderContext.height, listOf(GlGpuTexture.Preset.RGBNoFilter), true)) ?: throw SkipRender
            fb.exec { block() }
            m["colorTexture"] = fb.colorTextures[0]
            m["depthTexture"] = fb.depthTexture
            return fb
        }
    }

    private fun renderToEnv(uniforms: MutableMap<String, Any?>, captureContext: CaptureContext) {
        renderShadows(uniforms, true)
        val probeFb = inventory.cubeFrameBuffer(CubeFrameBufferDeclaration("probe-$envSlot", captureContext.resolution, captureContext.resolution, true)) ?: throw SkipRender
        val probeUniforms = mutableMapOf<String, Any?>()
        probeUniforms += uniforms
        val projection = FrustumProjection(2f * captureContext.near, 2f * captureContext.near, captureContext.near, captureContext.far)
        mapOf(
            GL_TEXTURE_CUBE_MAP_NEGATIVE_X to DefaultCamera(captureContext.position, -1.x, -1.y),
            GL_TEXTURE_CUBE_MAP_NEGATIVE_Y to DefaultCamera(captureContext.position, -1.y, -1.z),
            GL_TEXTURE_CUBE_MAP_NEGATIVE_Z to DefaultCamera(captureContext.position, -1.z, -1.y),
            GL_TEXTURE_CUBE_MAP_POSITIVE_X to DefaultCamera(captureContext.position, 1.x, -1.y),
            GL_TEXTURE_CUBE_MAP_POSITIVE_Y to DefaultCamera(captureContext.position, 1.y, 1.z),
            GL_TEXTURE_CUBE_MAP_POSITIVE_Z to DefaultCamera(captureContext.position, 1.z, -1.y),
        ).forEach {
            probeUniforms["view"] = it.value.mat4
            probeUniforms["projection"] = projection.mat4
            probeUniforms["cameraPos"] = it.value.position
            probeUniforms["cameraDir"] = it.value.direction
            probeFb.exec(it.key) {
                renderForwardOpaques(probeUniforms, captureContext.resolution, captureContext.resolution)
                renderTransparents(probeUniforms, it.value)
            }
        }
        probeFb.finish()
        uniforms["envTexture$envSlot"] = probeFb.colorTexture
        uniforms["envDepthTexture$envSlot"] = probeFb.depthTexture
    }
}

internal object SkipRender : RuntimeException()

