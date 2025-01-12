package com.zakgof.korender.impl.engine

import com.zakgof.korender.TouchEvent
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.impl.engine.shadow.ShadowRenderer
import com.zakgof.korender.impl.engine.shadow.ShadowerData
import com.zakgof.korender.impl.geometry.ScreenQuad
import com.zakgof.korender.impl.gl.GL.glBlendFunc
import com.zakgof.korender.impl.gl.GL.glClear
import com.zakgof.korender.impl.gl.GL.glClearColor
import com.zakgof.korender.impl.gl.GL.glCullFace
import com.zakgof.korender.impl.gl.GL.glDepthFunc
import com.zakgof.korender.impl.gl.GL.glDepthMask
import com.zakgof.korender.impl.gl.GL.glEnable
import com.zakgof.korender.impl.gl.GL.glViewport
import com.zakgof.korender.impl.gl.GLConstants.GL_BACK
import com.zakgof.korender.impl.gl.GLConstants.GL_BLEND
import com.zakgof.korender.impl.gl.GLConstants.GL_COLOR_BUFFER_BIT
import com.zakgof.korender.impl.gl.GLConstants.GL_CULL_FACE
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_BUFFER_BIT
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_TEST
import com.zakgof.korender.impl.gl.GLConstants.GL_LEQUAL
import com.zakgof.korender.impl.gl.GLConstants.GL_ONE_MINUS_SRC_ALPHA
import com.zakgof.korender.impl.gl.GLConstants.GL_SRC_ALPHA
import com.zakgof.korender.impl.glgpu.ColorList
import com.zakgof.korender.impl.glgpu.GlGpuFrameBuffer
import com.zakgof.korender.impl.glgpu.GlGpuTextureList
import com.zakgof.korender.impl.glgpu.IntList
import com.zakgof.korender.impl.glgpu.Mat4List
import com.zakgof.korender.impl.glgpu.Vec3List
import com.zakgof.korender.impl.gltf.GltfSceneBuilder
import com.zakgof.korender.impl.material.InternalTexture
import com.zakgof.korender.impl.material.MaterialBuilder
import com.zakgof.korender.impl.material.NotYetLoadedTexture
import com.zakgof.korender.impl.material.materialDeclaration
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Vec3
import kotlin.math.min

internal class Scene(
    private val sceneDeclaration: SceneDeclaration,
    private val inventory: Inventory,
    private val renderContext: RenderContext,
    time: Float
) {

    private val deferredShading = false

    private val shadowCasters: List<Renderable>
    val touchBoxesHandler: (TouchEvent) -> Boolean
    private val touchBoxes = mutableListOf<TouchBox>()

    private val opaques = mutableListOf<Renderable>()
    private val transparents = mutableListOf<Renderable>()
    private val skies = mutableListOf<Renderable>()
    private val screens = mutableListOf<Renderable>()

    // TODO: ugly
    private val fixer = { value: Any? ->
        if (value is InternalTexture) {
            inventory.texture(value) ?: NotYetLoadedTexture
        } else
            value
    }

    init {
        sceneDeclaration.gltfs.forEach {
            inventory.gltf(it)?.let { l ->
                sceneDeclaration.renderables += GltfSceneBuilder(inventory, it.gltfResource, it.transform, l).build(time)
            }
        }
        sceneDeclaration.renderables.forEach {
            val renderable = Renderable.create(inventory, it, renderContext.camera)
            renderable?.let { r ->
                when (it.bucket) {
                    Bucket.OPAQUE -> opaques.add(r)
                    Bucket.SKY -> skies.add(r)
                    Bucket.TRANSPARENT -> transparents.add(r)
                    Bucket.SCREEN -> screens.add(r)
                }
            }
        }
        val guiRenderers = sceneDeclaration.guis.map {
            GuiRenderer(inventory, renderContext.width, renderContext.height, it)
        }
        screens.addAll(guiRenderers.flatMap { it.renderables })
        touchBoxes.addAll(guiRenderers.flatMap { it.touchBoxes })
        touchBoxesHandler = { evt ->
            touchBoxes.any { it.touch(evt) }
        }
        shadowCasters = createShadowCasters(sceneDeclaration.renderables)
    }

    private fun createShadowCasters(declarations: List<RenderableDeclaration>) =
        // TODO: renderable or material flag to disable shadow casting
        declarations.filter {
            (it.shader.fragFile == "!shader/geometry.frag" ||
                    it.shader.fragFile == "!shader/forward.frag")
        }
            .mapNotNull { Renderable.createShadowCaster(inventory, it) }

    fun render() {

        val directLightUniforms = renderShadows()
        val contextUniforms = renderContext.uniforms()
        val geometryUniforms = renderDeferred(contextUniforms)

        val filterFrameBuffers = (0 until min(sceneDeclaration.filters.size, 2))
            .map {
                inventory.frameBuffer(FrameBufferDeclaration("filter-$it", renderContext.width, renderContext.height, 1, true))
            }

        val prevFrameContext = mutableMapOf<String, Any?>()

        val mainFrameBuffer = if (sceneDeclaration.filters.isEmpty()) null else filterFrameBuffers[0]

        val totalMainUniforms = contextUniforms + geometryUniforms + directLightUniforms
        renderMain(mainFrameBuffer, totalMainUniforms)

        prevFrameContext["filterColorTexture"] = mainFrameBuffer?.colorTextures?.get(0) ?: contextUniforms["noiseTexture"] // TODO this is hack
        prevFrameContext["filterDepthTexture"] = mainFrameBuffer?.depthTexture ?: contextUniforms["noiseTexture"] // TODO this is hack

        sceneDeclaration.filters.forEachIndexed { p, filter ->
            val totalContextUniforms = contextUniforms + geometryUniforms + directLightUniforms + prevFrameContext
            val frameBuffer = if (p == sceneDeclaration.filters.size - 1) null else filterFrameBuffers[(p+1) % 2]
            renderTo(frameBuffer) {
                renderFilter(filter, totalContextUniforms)
                if (frameBuffer == null) {
                    glEnable(GL_BLEND)
                    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
                    screens.forEach { it.render(totalContextUniforms, fixer) }
                    glDepthMask(false)
                    transparents.sortedByDescending { (renderContext.camera.mat4 * it.transform.offset()).z }
                        .forEach { it.render(totalContextUniforms, fixer) }
                    glDepthMask(true)
                }
            }
            prevFrameContext["filterColorTexture"] = frameBuffer?.colorTextures?.get(0) ?: contextUniforms["noiseTexture"] // TODO this is hack
            prevFrameContext["filterDepthTexture"] = frameBuffer?.depthTexture ?: contextUniforms["noiseTexture"] // TODO this is hack
        }
    }

    private fun renderDeferred(contextUniforms: Map<String, Any?>): Map<String, Any?> {
        if (deferredShading) {
            // TODO: configurable texture channels resolutions (half/quarter)
            val geometryBuffer = inventory.frameBuffer(FrameBufferDeclaration("geometry", renderContext.width, renderContext.height, 3, true)) ?: return mapOf()
            geometryBuffer.exec {
                renderGeometry(contextUniforms)
            }
            return mapOf(
                "cdiffTexture" to geometryBuffer.colorTextures[0],
                "normalTexture" to geometryBuffer.colorTextures[1],
                "materialTexture" to geometryBuffer.colorTextures[2],
                "depthTexture" to geometryBuffer.depthTexture!!
            )
        }
        return mapOf()
    }

    private fun renderMain(frameBuffer: GlGpuFrameBuffer?, uniforms: Map<String, Any?>) {
        renderTo (frameBuffer) {
            if (deferredShading) {
                renderFilter(
                    materialDeclaration(MaterialBuilder(vertShaderFile = "!shader/screen.vert", fragShaderFile = "!shader/composition.frag")),
                    uniforms
                )
            } else {
                renderGeometry(uniforms)
            }
            skies.forEach { it.render(uniforms, fixer) }

            if (frameBuffer == null) {
                glEnable(GL_BLEND)
                screens.forEach { it.render(uniforms, fixer) }
                glDepthMask(false)
                transparents.sortedByDescending { (renderContext.camera.mat4 * it.transform.offset()).z }
                    .forEach { it.render(uniforms, fixer) }
                glDepthMask(true)
            }
        }
    }

    private fun renderShadows(): Map<String, Any?> {
        val uniforms = mutableMapOf<String, Any?>()

//        (0 until 32).forEach { _ ->
//            uniforms["directionalLightsDir[0]"] = Vec3List(listOf())
//            uniforms["directionalLightColor[0]"] = ColorList(listOf())
//            uniforms["directionalLightShadowTextureIndex[0]"] = -1
//            uniforms["directionalLightShadowTextureCount[0]"] = 0
//            uniforms["pointLightPos[0]"] = Vec3List(listOf())
//            uniforms["pointLightColor[0]"] = ColorList(listOf())
//            uniforms["bsps[0]"] = Mat4List(listOf())
//            uniforms["shadowTextures[0]"] = GlGpuTextureList(listOf())
//        }

        val shadowData = mutableListOf<ShadowerData>()
        val directionalDirs = mutableListOf<Vec3>()
        val directionalColors = mutableListOf<Color>()
        val directionalShadowIndexes = mutableListOf<Int>()
        val directionalShadowCounts = mutableListOf<Int>()
        sceneDeclaration.directionalLights.forEachIndexed { li, dl ->
            val indexes = dl.shadowDeclaration.cascades.mapIndexedNotNull { ci, cascadeDeclaration ->
                ShadowRenderer.render(
                    "$li-$ci",
                    inventory,
                    dl.direction,
                    cascadeDeclaration,
                    renderContext,
                    shadowCasters,
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
        uniforms["numShadows"] = shadowData.size
        uniforms["shadowTextures[0]"] = GlGpuTextureList(shadowData.map { it.texture })
        uniforms["bsps[0]"] = Mat4List(shadowData.map { it.bsp })

        uniforms["numDirectionalLights"] = sceneDeclaration.directionalLights.size
        uniforms["directionalLightDir[0]"] = Vec3List(directionalDirs)
        uniforms["directionalLightColor[0]"] = ColorList(directionalColors)
        uniforms["directionalLightShadowTextureIndex[0]"] = IntList(directionalShadowIndexes)
        uniforms["directionalLightShadowTextureCount[0]"] = IntList(directionalShadowCounts)

        uniforms["ambientColor"] = sceneDeclaration.ambientLightColor

        uniforms["numPointLights"] = sceneDeclaration.pointLights.size
        uniforms["pointLightPos[0]"] = Vec3List(sceneDeclaration.pointLights.map { it.position })
        uniforms["pointLightColor[0]"] = ColorList(sceneDeclaration.pointLights.map { it.color })

        return uniforms
    }

    private fun renderFilter(filter: MaterialDeclaration, uniforms: Map<String, Any?>) {

        val mesh = inventory.mesh(ScreenQuad)
        val shader = inventory.shader(filter.shader)

        // TODO
        val back = renderContext.backgroundColor
        glClearColor(back.r, back.g, back.b, back.a)
        glViewport(0, 0, renderContext.width, renderContext.height)
        glEnable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDepthFunc(GL_LEQUAL)
        glEnable(GL_CULL_FACE)
        glCullFace(GL_BACK)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        if (mesh != null && shader != null) {
            Renderable(mesh, shader, filter.uniforms).render(uniforms, fixer)
        }
    }

    private fun renderTo(fb: GlGpuFrameBuffer?, block: () -> Unit) {
        if (fb == null) block() else fb.exec { block() }
    }

    private fun renderGeometry(contextUniforms: Map<String, Any?>) {
        val back = renderContext.backgroundColor
        glClearColor(back.r, back.g, back.b, back.a)
        glViewport(0, 0, renderContext.width, renderContext.height)
        glEnable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDepthFunc(GL_LEQUAL)
        glEnable(GL_CULL_FACE)
        glCullFace(GL_BACK)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        opaques.forEach { it.render(contextUniforms, fixer) }
    }

    internal class TouchBox(
        private val x: Int,
        private val y: Int,
        private val w: Int,
        private val h: Int,
        private val handler: TouchHandler
    ) {
        fun touch(touchEvent: TouchEvent): Boolean {
            // TODO: process drag-out as UP
            if (touchEvent.x > x && touchEvent.x < x + w && touchEvent.y > y && touchEvent.y < y + h) {
                handler(touchEvent)
                return true
            }
            return false
        }
    }
}

