package com.zakgof.korender

import com.zakgof.korender.com.zakgof.korender.DefaultCamera
import com.zakgof.korender.glgpu.GlGpu
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
        platform.run(800, 600, {block.invoke(this)}, this::frame)
    }

    private fun frame() {
        context.put("view", camera.mat4())
        context.put("projection", projection.mat4())

        val t1 = projection.mat4().project(camera.mat4() * Vec3.ZERO)
        val t2 = projection.mat4().project(camera.mat4() * Vec3.X)
        val t3 = projection.mat4().project(camera.mat4() * Vec3.Y)

        onFrame.invoke()
        renderables.forEach { it.render() }
    }
}
