package com.zakgof.korender.impl.engine

import com.zakgof.korender.TouchHandler
import com.zakgof.korender.camera.Camera
import com.zakgof.korender.impl.engine.shadow.CascadeShadower
import com.zakgof.korender.impl.engine.shadow.Shadower
import com.zakgof.korender.impl.gpu.GpuFrameBuffer
import com.zakgof.korender.input.TouchEvent
import com.zakgof.korender.material.TextureDeclaration
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.projection.Projection
import com.zakgof.korender.uniforms.UniformSupplier

internal class Scene(sceneDeclaration: SceneDeclaration, private val inventory: Inventory, private val camera: Camera, private val width: Int, private val height: Int) {

    private val shadower: Shadower?
    private val passes: List<ScenePass>
    private val shadowCasters: List<Renderable>
    private val touchBoxes: List<TouchBox>
    val touchBoxesHandler:(TouchEvent) -> Boolean

    init {
        println("cp3.1")
        sceneDeclaration.compilePasses()
        println("cp3.2")
        shadower = sceneDeclaration.shadow?.let { CascadeShadower(inventory, it.cascades) }
        val shadowCascades = shadower?.cascadeNumber ?: 0
        shadowCasters = if (sceneDeclaration.passes.isNotEmpty()) createShadowCasters(sceneDeclaration.passes[0].renderables) else listOf()
        println("cp3.3")
        passes = sceneDeclaration.passes.map { ScenePass(inventory, camera, width, height, it, shadowCascades) }
        println("cp3.4")
        touchBoxes = passes.flatMap { it.touchBoxes }
        touchBoxesHandler = { evt ->
            touchBoxes.any { it.touch(evt) }
        }
    }

    private fun createShadowCasters(declarations: List<RenderableDeclaration>) =
        declarations.filter { it.shader.fragFile == "standart.frag" && !it.shader.defs.contains("NO_SHADOW_CAST") }
            .map { Renderable.create(inventory, it, camera, true) }

    fun render(context: Map<String, Any?>, projection: Projection, camera: Camera, light: Vec3) {
        println("cp r.1")
        val shadowUniforms: UniformSupplier = shadower?.render(projection, camera, light, shadowCasters) ?: UniformSupplier { null }
        println("cp r.2")
        val passFrameBuffers = (0 until passes.size - 1)
            .map { inventory.frameBuffer(FrameBufferDeclaration("filter-$it", width, height, true)) }

        println("cp r.3")
        val prevFrameContext = mutableMapOf<String, Any?>()
        val uniformDecorator: (UniformSupplier) -> UniformSupplier = {
            UniformSupplier { key ->
                var value = it[key] ?: context[key] ?: shadowUniforms[key] ?: prevFrameContext[key]
                if (value is TextureDeclaration) {
                    value = inventory.texture(value)
                }
                value
            }
        }
        for (p in passes.indices) {
            val frameBuffer = if (p == passes.size - 1) null else passFrameBuffers[p % 2]
            println("cp r.4")
            renderTo(frameBuffer) {
                passes[p].render(uniformDecorator)
            }
            prevFrameContext["filterColorTexture"] = frameBuffer?.colorTexture
            prevFrameContext["filterDepthTexture"] = frameBuffer?.depthTexture
        }
    }

    private fun renderTo(fb: GpuFrameBuffer?, block: () -> Unit) {
        if (fb == null) block() else fb.exec { block() }
    }

    internal class TouchBox(private val x: Int, private val y: Int, private val w: Int, private val h: Int, private val handler: TouchHandler) {
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

