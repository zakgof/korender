package com.zakgof.korender

import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.glgpu.GlGpu
import com.zakgof.korender.gpu.GpuMesh
import com.zakgof.korender.gpu.GpuShader
import com.zakgof.korender.material.GpuMaterial
import com.zakgof.korender.material.MapUniformSupplier
import com.zakgof.korender.material.UniformSupplier
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.projection.OrthoProjection
import com.zakgof.korender.projection.Projection
import java.lang.System.nanoTime
import java.util.*

fun korender(platform: Platform, block: KorenderContext.() -> Unit): Unit = KorenderContext(platform).start(block)

class KorenderContext(val platform: Platform, var width: Int = 1280, var height: Int = 800) {

    private val renderables = mutableListOf<Renderable>()
    private val context = mutableMapOf<String, Any>()
    private val contextUniforms = MapUniformSupplier(context)

    private var startNanos: Long = 0
    private var prevFrameNano: Long = 0
    private val frames: Queue<Long> = LinkedList()

    val gpu: Gpu = GlGpu()
    var onFrame: KorenderContext.(FrameInfo) -> Unit = {}
    var onResize: KorenderContext.() -> Unit = {}
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
        startNanos = nanoTime()
        prevFrameNano = nanoTime()
        platform.run(
            width = width,
            height = height,
            init = {
                block.invoke(this)
                onResize.invoke(this)
            },
            onFrame = this::frame,
            onResize = this::resize
        )
    }

    private fun resize(w: Int, h: Int) {
        width = w
        height = h
        onResize.invoke(this)
    }

    private fun frame() {
        val now = nanoTime()
        updateContext()
        val frameTime = now - prevFrameNano
        frames.add(frameTime)
        val frameInfo = FrameInfo(now - startNanos, frameTime, calcAverageFps())
        prevFrameNano = now
        onFrame.invoke(this, frameInfo)
        renderables.forEach { it.render() }
    }

    private fun calcAverageFps(): Double {
        while (frames.size > 8) {
            frames.poll()
        }
        return 1e9 / frames.average()
    }

    private fun updateContext() {
        context.run {
            put("view", camera.mat4())
            put("projection", projection.mat4())
            put("cameraPos", camera.position())
        }
    }

    fun renderable(gpuMesh: GpuMesh, gpuShader: GpuShader, material: UniformSupplier): Renderable =
        Renderable(gpuMesh, gpuShader, UniformSupplier { material[it] ?: contextUniforms[it] })

    fun renderable(gpuMesh: GpuMesh, gpuMaterial: GpuMaterial): Renderable =
        Renderable(gpuMesh, gpuMaterial.shader, UniformSupplier {  gpuMaterial.uniforms[it] ?: contextUniforms[it] })

}


