package com.zakgof.korender.impl.engine

import com.zakgof.korender.TouchEvent
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.impl.engine.shadow.CascadeShadower
import com.zakgof.korender.impl.engine.shadow.Shadower
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
import com.zakgof.korender.impl.glgpu.GlGpuFrameBuffer
import com.zakgof.korender.impl.gltf.GltfSceneBuilder
import com.zakgof.korender.impl.material.InternalTexture
import com.zakgof.korender.impl.material.NotYetLoadedTexture
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Vec3

internal class Scene(
    private val sceneDeclaration: SceneDeclaration,
    private val inventory: Inventory,
    private val renderContext: RenderContext,
    time: Float
) {

    private val shadower: Shadower?
    private val shadowCasters: List<Renderable>
    val touchBoxesHandler: (TouchEvent) -> Boolean
    private val touchBoxes = mutableListOf<TouchBox>()

    private val opaques = mutableListOf<Renderable>()
    private val transparents = mutableListOf<Renderable>()
    private val skies = mutableListOf<Renderable>()
    private val screens = mutableListOf<Renderable>()
    private val lightUniforms = mutableMapOf<String, Any?>()

    init {
        fillLightUniforms(sceneDeclaration)

        sceneDeclaration.gltfs.forEach {
            inventory.gltf(it)?.let { l ->
                sceneDeclaration.renderables += GltfSceneBuilder(inventory, it.gltfResource, it.transform, l).build(time)
            }
        }
        sceneDeclaration.renderables.forEach {
            val renderable = Renderable.create(inventory, it, renderContext.camera, false, sceneDeclaration.shadow?.cascades?.size ?:0)
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

        shadower = sceneDeclaration.shadow?.let { CascadeShadower(inventory, it.cascades) }
        shadowCasters = createShadowCasters(sceneDeclaration.renderables)
    }

    private fun fillLightUniforms(sceneDeclaration: SceneDeclaration) {
        if (sceneDeclaration.directionalLights.isEmpty() && sceneDeclaration.pointLights.isEmpty()) {
            sceneDeclaration.directionalLights.add(DirectionalLightDeclaration(Vec3(1f, -1f, 1f).normalize(), Color(1f, 7f, 7f, 7f)))
        }
        lightUniforms["ambientColor"] = sceneDeclaration.ambientLightColor
        lightUniforms["numDirectionalLights"] = sceneDeclaration.directionalLights.size
        (0 until 32).forEach { i ->
            lightUniforms["directionalLights[$i].dir"] = if (i < sceneDeclaration.directionalLights.size) sceneDeclaration.directionalLights[i].direction else Vec3.ZERO
            lightUniforms["directionalLights[$i].color"] = if (i < sceneDeclaration.directionalLights.size) sceneDeclaration.directionalLights[i].color else Color.White
        }
        lightUniforms["numPointLights"] = sceneDeclaration.pointLights.size
        (0 until 32).forEach { i ->
            lightUniforms["pointLights[$i].pos"] = if (i < sceneDeclaration.pointLights.size) sceneDeclaration.pointLights[i].position else Vec3.ZERO
            lightUniforms["pointLights[$i].color"] = if (i < sceneDeclaration.pointLights.size) sceneDeclaration.pointLights[i].color else Color.White
        }
    }

    private fun createShadowCasters(declarations: List<RenderableDeclaration>) =
        declarations.filter {
            it.shader.fragFile == "!shader/geometry.frag" && !it.shader.defs.contains(
                "NO_SHADOW_CAST"
            )
        }
            .mapNotNull { Renderable.create(inventory, it, renderContext.camera, true) }

    fun render() {

        // TODO: ugly
        val fixer = { value: Any? ->
            if (value is InternalTexture) {
                inventory.texture(value) ?: NotYetLoadedTexture
            } else
                value
        }

        // TODO: shadow pass per light
        val shadowUniforms: Map<String, Any?> =
            shadower?.render(renderContext, sceneDeclaration.directionalLights[0].direction, shadowCasters, fixer) ?: mapOf()

        // TODO: configurable texture channels resolutions (half/quarter)
        val geometryBuffer = inventory.frameBuffer(FrameBufferDeclaration("geometry", renderContext.width, renderContext.height, 3, true)) ?: return

        val contextUniforms = renderContext.uniforms()

        geometryBuffer.exec {
            renderGeometry(contextUniforms, fixer)
        }
        val geometryUniforms = mapOf(
            "albedoTexture" to geometryBuffer.colorTextures[0],
            "normalTexture" to geometryBuffer.colorTextures[1],
            "materialTexture" to geometryBuffer.colorTextures[2],
            "depthTexture" to geometryBuffer.depthTexture!!
        )

        val filterFrameBuffers = (0 until sceneDeclaration.filters.size - 1)
            .map {
                inventory.frameBuffer(FrameBufferDeclaration("filter-$it", renderContext.width, renderContext.height, 1, true))
            }

        val prevFrameContext = mutableMapOf<String, Any?>()

        sceneDeclaration.filters.forEachIndexed { p, filter ->

            val totalContextUniforms = contextUniforms + geometryUniforms + shadowUniforms + lightUniforms + prevFrameContext

            val frameBuffer = if (p == sceneDeclaration.filters.size - 1) null else filterFrameBuffers[p % 2]
            renderTo(frameBuffer) {
                renderFilter(filter, totalContextUniforms, fixer, frameBuffer == null)
            }
            prevFrameContext["filterColorTexture"] = frameBuffer?.colorTextures?.get(0) ?: contextUniforms["noiseTexture"] // TODO this is hack
            prevFrameContext["filterDepthTexture"] = frameBuffer?.depthTexture ?: contextUniforms["noiseTexture"] // TODO this is hack
        }
    }

    private fun renderFilter(filter: MaterialDeclaration, uniforms: Map<String, Any?>, fixer: (Any?) -> Any?, final: Boolean) {

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

        if (final) {
            screens.forEach { it.render(uniforms, fixer) }
            glDepthMask(false)
            transparents.sortedByDescending { (renderContext.camera.mat4 * it.transform.offset()).z }
                .forEach { it.render(uniforms, fixer) }
            glDepthMask(true)
        }

    }

    private fun renderTo(fb: GlGpuFrameBuffer?, block: () -> Unit) {
        if (fb == null) block() else fb.exec { block() }
    }

    private fun renderGeometry(contextUniforms: Map<String, Any?>, fixer: (Any?) -> Any?) {
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
        skies.forEach { it.render(contextUniforms, fixer) }
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

