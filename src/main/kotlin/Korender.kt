package com.zakgof.korender

import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.glgpu.GlGpu
import com.zakgof.korender.projection.OrthoProjection
import com.zakgof.korender.projection.Projection
import math.Vec3

fun korender(platform: Platform, block: KorenderContext.() -> Unit): Unit = KorenderContext(platform).start(block)

class KorenderContext(val platform: Platform) {

    private val renderables = mutableListOf<Renderable>()
    private val context = mutableMapOf<String, Any>()
    private val contextUniforms = MapUniformSupplier(context)

    val gpu: Gpu = GlGpu()
    var onFrame: () -> Unit = {}
    var camera: DefaultCamera = DefaultCamera(
        pos = Vec3(0f, 5f, 15f),
        dir = Vec3(0f, 0f, -1f),
        up = Vec3(0f, 1f, 0f)
    )
    var projection: Projection = OrthoProjection(
        width = 10f,
        height = 10f,
        near = 10f,
        far = 10000f
    )

    fun add(renderable: Renderable) {
        renderables.add(renderable) // TODO standard buckets
    }

    fun withContext(material: UniformSupplier): UniformSupplier =
        UniformSupplier { material[it] ?: contextUniforms[it] }

    fun start(block: KorenderContext.() -> Unit) {
        platform.run(1280, 800, {block.invoke(this)}, this::frame)
    }

    private fun frame() {
        updateContext()
        onFrame.invoke()
        renderables.forEach { it.render() }
    }

    private fun updateContext() {
        context.run {
            put("view", camera.mat4())
            put("projection", projection.mat4())
        }
    }
}
