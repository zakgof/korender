package com.zakgof.korender.impl.engine

import com.zakgof.korender.TouchEvent
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.impl.engine.shadow.ShadowRenderer
import com.zakgof.korender.impl.engine.shadow.ShadowerData
import com.zakgof.korender.impl.engine.shadow.uniforms
import com.zakgof.korender.impl.geometry.ScreenQuad
import com.zakgof.korender.impl.gl.GL.glClear
import com.zakgof.korender.impl.gl.GL.glClearColor
import com.zakgof.korender.impl.gl.GL.glDepthFunc
import com.zakgof.korender.impl.gl.GL.glDepthMask
import com.zakgof.korender.impl.gl.GL.glEnable
import com.zakgof.korender.impl.gl.GL.glViewport
import com.zakgof.korender.impl.gl.GLConstants.GL_BLEND
import com.zakgof.korender.impl.gl.GLConstants.GL_COLOR_BUFFER_BIT
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_BUFFER_BIT
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_TEST
import com.zakgof.korender.impl.gl.GLConstants.GL_LEQUAL
import com.zakgof.korender.impl.glgpu.ColorList
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.glgpu.IntList
import com.zakgof.korender.impl.glgpu.Vec3List
import com.zakgof.korender.impl.gltf.GltfSceneBuilder
import com.zakgof.korender.impl.material.InternalMaterialModifier
import com.zakgof.korender.impl.material.InternalTexture
import com.zakgof.korender.impl.material.MaterialBuilder
import com.zakgof.korender.impl.material.NotYetLoadedTexture
import com.zakgof.korender.impl.material.materialDeclaration
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Vec3

internal class Scene(
    private val sceneDeclaration: SceneDeclaration,
    private val inventory: Inventory,
    private val renderContext: RenderContext,
    private val deferredShading: Boolean,
    time: Float
) {

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
                sceneDeclaration.renderables += GltfSceneBuilder(it.gltfResource, it.transform, l, deferredShading).build(time)
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
    }

    fun render() {

        val uniforms = mutableMapOf<String, Any?>()

        renderContext.uniforms(uniforms)
        renderShadows(uniforms)

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

        uniforms += renderDeferredOpaques(uniforms)

        if (sceneDeclaration.filters.isNotEmpty()) {
            renderToFilter(0, uniforms) {
                renderComposition(uniforms)
            }
        } else {
            renderComposition(uniforms)
            renderTransparents(uniforms)
        }

        sceneDeclaration.filters.forEachIndexed { index, filter ->
            if (index < sceneDeclaration.filters.size - 1) {
                renderToFilter(index + 1, uniforms) {
                    renderFilter(filter, uniforms)
                }
            } else {
                renderFilter(filter, uniforms)
                renderTransparents(uniforms)
            }
        }
    }

    private fun renderSceneForward(uniforms: MutableMap<String, Any?>) {
        if (sceneDeclaration.filters.isEmpty()) {
            renderForwardOpaques(uniforms)
            renderTransparents(uniforms)
        } else {
            renderToFilter(0, uniforms) {
                renderForwardOpaques(uniforms)
            }
            sceneDeclaration.filters.dropLast(1).forEachIndexed { index, filter ->
                renderToFilter(index + 1, uniforms) {
                    renderFilter(filter, uniforms)
                }
            }
            renderFilter(sceneDeclaration.filters.last(), uniforms)
            renderTransparents(uniforms)
        }
    }

    private fun renderDeferredOpaques(uniforms: Map<String, Any?>): Map<String, Any?> {
        // TODO: configurable texture channels resolutions (half/quarter)
        val geometryBuffer = inventory.frameBuffer(
            FrameBufferDeclaration(
                "geometry", renderContext.width, renderContext.height,
                listOf(GlGpuTexture.Preset.RGBANoFilter, GlGpuTexture.Preset.RGBANoFilter, GlGpuTexture.Preset.RGBANoFilter),
                true
            )
        ) ?: throw SkipRender
        geometryBuffer.exec {
            glClearColor(0f, 0f, 0f, 1f)
            glViewport(0, 0, renderContext.width, renderContext.height)
            glEnable(GL_DEPTH_TEST)
            glDepthFunc(GL_LEQUAL)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            opaques.forEach { it.render(uniforms, fixer) }
        }
        return mapOf(
            "cdiffTexture" to geometryBuffer.colorTextures[0],
            "normalTexture" to geometryBuffer.colorTextures[1],
            "materialTexture" to geometryBuffer.colorTextures[2],
            "depthTexture" to geometryBuffer.depthTexture!!
        )
    }

    private fun renderComposition(uniforms: MutableMap<String, Any?>) {
        val back = renderContext.backgroundColor
        glClearColor(back.r, back.g, back.b, back.a)
        glViewport(0, 0, renderContext.width, renderContext.height)
        glEnable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LEQUAL)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        renderFilter(
            materialDeclaration(
                MaterialBuilder(false),
                InternalMaterialModifier {
                    it.vertShaderFile = "!shader/screen.vert"
                    it.fragShaderFile = "!shader/composition.frag"
                }
            ),
            uniforms
        )
        skies.forEach { it.render(uniforms, fixer) }
    }

    private fun renderForwardOpaques(uniforms: Map<String, Any?>) {
        val back = renderContext.backgroundColor
        glClearColor(back.r, back.g, back.b, back.a)
        glViewport(0, 0, renderContext.width, renderContext.height)
        glEnable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LEQUAL)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        opaques.forEach { it.render(uniforms, fixer) }
        skies.forEach { it.render(uniforms, fixer) }
    }

    private fun renderTransparents(uniforms: MutableMap<String, Any?>) {
        glEnable(GL_BLEND)
        screens.forEach { it.render(uniforms, fixer) }
        glDepthMask(false)
        transparents.sortedByDescending { (renderContext.camera.mat4 * it.transform.offset()).z }
            .forEach { it.render(uniforms, fixer) }
        glDepthMask(true)
    }

    private fun renderShadows(m: MutableMap<String, Any?>) {
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
        m["directionalLightColor[0]"] = ColorList(directionalColors)
        m["directionalLightShadowTextureIndex[0]"] = IntList(directionalShadowIndexes)
        m["directionalLightShadowTextureCount[0]"] = IntList(directionalShadowCounts)
        m["ambientColor"] = sceneDeclaration.ambientLightColor
        m["numPointLights"] = sceneDeclaration.pointLights.size
        m["pointLightPos[0]"] = Vec3List(sceneDeclaration.pointLights.map { it.position })
        m["pointLightColor[0]"] = ColorList(sceneDeclaration.pointLights.map { it.color })
    }

    private fun renderFilter(filter: MaterialDeclaration, uniforms: Map<String, Any?>) {
        val mesh = inventory.mesh(ScreenQuad)
        val shader = inventory.shader(filter.shader)
        glClearColor(0f, 0f, 0f, 1f)
        glViewport(0, 0, renderContext.width, renderContext.height)
        glEnable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LEQUAL)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        if (mesh != null && shader != null) {
            Renderable(mesh, shader, filter.uniforms).render(uniforms, fixer)
        }
    }

    private fun renderToFilter(index: Int, m: MutableMap<String, Any?>, block: () -> Unit) {
        val number = index % 2
        val fb = inventory.frameBuffer(FrameBufferDeclaration("filter-$number", renderContext.width, renderContext.height, listOf(GlGpuTexture.Preset.RGBNoFilter), true)) ?: throw SkipRender
        fb.exec { block() }
        m["filterColorTexture"] = fb.colorTextures[0]
        m["filterDepthTexture"] = fb.depthTexture
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

internal object SkipRender : RuntimeException()

