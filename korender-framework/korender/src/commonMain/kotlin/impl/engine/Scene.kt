package com.zakgof.korender.impl.engine

import com.zakgof.korender.TouchEvent
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.impl.engine.shadow.CascadeShadower
import com.zakgof.korender.impl.engine.shadow.Shadower
import com.zakgof.korender.impl.glgpu.GlGpuFrameBuffer
import com.zakgof.korender.impl.material.InternalTexture
import com.zakgof.korender.impl.material.NotYetLoadedTexture

internal class Scene(
    private val sceneDeclaration: SceneDeclaration,
    private val inventory: Inventory,
    private val renderContext: RenderContext,
    time: Float
) {

    private val shadower: Shadower?
    private val passes: List<ScenePass>
    private val shadowCasters: List<Renderable>
    private val touchBoxes: List<TouchBox>
    val touchBoxesHandler: (TouchEvent) -> Boolean

    init {
        sceneDeclaration.compilePasses()
        shadower = sceneDeclaration.shadow?.let { CascadeShadower(inventory, it.cascades) }
        val shadowCascades = shadower?.cascadeNumber ?: 0
        shadowCasters = if (sceneDeclaration.passes.isNotEmpty())
            createShadowCasters(sceneDeclaration.passes[0].renderables)
        else
            listOf()
        passes = sceneDeclaration.passes.map {
            ScenePass(inventory, renderContext, it, shadowCascades, time)
        }
        touchBoxes = passes.flatMap { it.touchBoxes }
        touchBoxesHandler = { evt ->
            touchBoxes.any { it.touch(evt) }
        }
    }

    private fun createShadowCasters(declarations: List<RenderableDeclaration>) =
        declarations.filter {
            it.shader.fragFile == "!shader/standart.frag" && !it.shader.defs.contains(
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
            shadower?.render(renderContext, sceneDeclaration.passes[0].directionalLights[0].direction, shadowCasters, fixer) ?: mapOf()
        val passFrameBuffers = (0 until passes.size - 1)
            .map {
                inventory.frameBuffer(FrameBufferDeclaration("filter-$it", renderContext.width, renderContext.height,true))
            }

        val prevFrameContext = mutableMapOf<String, Any?>()

        val contextUniforms = renderContext.uniforms()

        passes.forEachIndexed { p, pass ->

            val totalContextUniforms = contextUniforms + shadowUniforms + pass.lightUniforms + prevFrameContext

            val frameBuffer = if (p == passes.size - 1) null else passFrameBuffers[p % 2]
            renderTo(frameBuffer) {
                pass.render(totalContextUniforms, fixer)
            }
            prevFrameContext["filterColorTexture"] = frameBuffer?.colorTexture ?: contextUniforms["noiseTexture"] // TODO this is hack
            prevFrameContext["filterDepthTexture"] = frameBuffer?.depthTexture ?: contextUniforms["noiseTexture"] // TODO this is hack
        }
    }

    private fun renderTo(fb: GlGpuFrameBuffer?, block: () -> Unit) {
        if (fb == null) block() else fb.exec { block() }
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

