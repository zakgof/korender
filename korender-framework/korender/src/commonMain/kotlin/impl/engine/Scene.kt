package com.zakgof.korender.impl.engine

import com.zakgof.korender.TouchEvent
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.impl.engine.shadow.ShadowRenderer
import com.zakgof.korender.impl.engine.shadow.ShadowerData
import com.zakgof.korender.impl.geometry.ScreenQuad
import com.zakgof.korender.impl.gl.GL.glClear
import com.zakgof.korender.impl.gl.GL.glClearColor
import com.zakgof.korender.impl.gl.GL.glDepthFunc
import com.zakgof.korender.impl.gl.GL.glDepthMask
import com.zakgof.korender.impl.gl.GL.glDisable
import com.zakgof.korender.impl.gl.GL.glEnable
import com.zakgof.korender.impl.gl.GL.glViewport
import com.zakgof.korender.impl.gl.GLConstants.GL_BLEND
import com.zakgof.korender.impl.gl.GLConstants.GL_COLOR_BUFFER_BIT
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_BUFFER_BIT
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_TEST
import com.zakgof.korender.impl.gl.GLConstants.GL_LEQUAL
import com.zakgof.korender.impl.glgpu.ColorList
import com.zakgof.korender.impl.glgpu.GlGpuTextureList
import com.zakgof.korender.impl.glgpu.IntList
import com.zakgof.korender.impl.glgpu.Mat4List
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

        val uniforms = mutableMapOf<String, Any?>()

        uniforms += renderContext.uniforms()
        uniforms += renderShadows()

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
            uniforms += renderToFilter(0) {
                renderComposition(uniforms)
            }
        } else {
            renderComposition(uniforms)
            renderTransparents(uniforms)
        }

        sceneDeclaration.filters.forEachIndexed { index, filter ->
            if (index < sceneDeclaration.filters.size - 1) {
                uniforms += renderToFilter(index + 1) {
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
            uniforms += renderToFilter(0) {
                renderForwardOpaques(uniforms)
            }
            sceneDeclaration.filters.dropLast(1).forEachIndexed { index, filter ->
                uniforms += renderToFilter(index + 1) {
                    renderFilter(filter, uniforms)
                }
            }
            renderFilter(sceneDeclaration.filters.last(), uniforms)
            renderTransparents(uniforms)
        }
    }

    private fun renderDeferredOpaques(uniforms: Map<String, Any?>): Map<String, Any?> {
        // TODO: configurable texture channels resolutions (half/quarter)
        val geometryBuffer = inventory.frameBuffer(FrameBufferDeclaration("geometry", renderContext.width, renderContext.height, 3, true)) ?: throw SkipRender
        geometryBuffer.exec {
            glClearColor(0f, 0f, 0f, 1f)
            glViewport(0, 0, renderContext.width, renderContext.height)
            glDisable(GL_BLEND)
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

    private fun renderShadows(): Map<String, Any?> {
        val uniforms = mutableMapOf<String, Any?>()
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

    private fun renderToFilter(index: Int, block: () -> Unit): Map<String, Any?> {
        val number = index % 2
        val fb = inventory.frameBuffer(FrameBufferDeclaration("filter-$number", renderContext.width, renderContext.height, 1, true)) ?: throw SkipRender
        fb.exec { block() }
        return mapOf(
            "filterColorTexture" to fb.colorTextures[0],
            "filterDepthTexture" to fb.depthTexture
        )
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

private object SkipRender : RuntimeException()

