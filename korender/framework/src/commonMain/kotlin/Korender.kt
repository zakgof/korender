package com.zakgof.korender

import androidx.compose.runtime.Composable
import com.zakgof.korender.camera.Camera
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.gl.VGL11
import com.zakgof.korender.glgpu.GlGpu
import com.zakgof.korender.gpu.Gpu
import com.zakgof.korender.gpu.GpuFrameBuffer
import com.zakgof.korender.gpu.GpuMesh
import com.zakgof.korender.material.MapUniformSupplier
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import com.zakgof.korender.projection.FrustumProjection
import com.zakgof.korender.projection.Projection
import com.zakgof.korender.shadow.Shadower
import com.zakgof.korender.shadow.SimpleShadower
import java.lang.System.nanoTime
import java.util.LinkedList
import java.util.Queue

@Composable
fun Korender(block: KorenderContext.() -> Unit) {

    lateinit var korender: KorenderContext

    getPlatform().openGL(
        init = {
            VGL11.glEnable(VGL11.GL_BLEND)
            VGL11.glBlendFunc(VGL11.GL_SRC_ALPHA, VGL11.GL_ONE_MINUS_SRC_ALPHA)
            VGL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
            korender = KorenderContext()
            block.invoke(korender)
            korender.onResize.invoke(korender)
        },
        frame = { korender.frame() },
        resize = { w, h -> korender.resize(w, h) }
    )
}

class KorenderContext(var width: Int = 100, var height: Int = 100) {

    private val filters = mutableListOf<Filter>()
    private val filterFrameBuffers = mutableListOf<GpuFrameBuffer>()

    private val opaques = mutableListOf<Renderable>()
    private val transparents = mutableListOf<Renderable>()
    private val skies = mutableListOf<Renderable>()
    private val screens = mutableListOf<Renderable>()

    private val context = mutableMapOf<String, Any?>()
    private val contextUniforms = MapUniformSupplier(context)

    private var frameNumber = 0L
    private var startNanos: Long = nanoTime()
    private var prevFrameNano: Long = nanoTime()
    private val frames: Queue<Long> = LinkedList()

    val gpu: Gpu = GlGpu()
    private val filterScreenQuad: GpuMesh = Meshes.screenQuad().build(gpu).gpuMesh

    var shadower: Shadower = SimpleShadower(gpu)
    var onFrame: KorenderContext.(FrameInfo) -> Unit = {}
    var onResize: KorenderContext.() -> Unit = {}
    var camera: Camera = DefaultCamera(20.z, -1.z, 1.y)
        set(value) {
            field = value
            updateContext()
        }
    var projection: Projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
        set(value) {
            field = value
            updateContext()
        }
    var light = Vec3(1f, -1f, 0f).normalize()

    fun add(renderable: Renderable, bucket: Bucket = Bucket.OPAQUE) {
        when (bucket) {
            Bucket.OPAQUE -> opaques.add(renderable)
            Bucket.TRANSPARENT -> transparents.add(renderable)
            Bucket.SKY -> skies.add(renderable)
            Bucket.SCREEN -> screens.add(renderable)
        }
    }

    init {
        VGL11.glEnable(VGL11.GL_DEPTH_TEST)
    }

    fun resize(w: Int, h: Int) {
        width = w
        height = h
        onResize.invoke(this)
    }

    fun frame() {
        val frameInfo = updateFrameInfo()
        updateContext()
        onFrame.invoke(this, frameInfo)
        renderShadowMap()

        if (filters.isEmpty()) {
            render()
        } else {
            for (p in 0..filters.size) {
                val frameBuffer = if (p == filters.size) null else filterFrameBuffers.get(p % 2)
                renderTo(frameBuffer) {
                    if (p == 0) {
                        VGL11.glClear(VGL11.GL_COLOR_BUFFER_BIT or VGL11.GL_DEPTH_BUFFER_BIT)
                        render()
                    } else {
                        val filter = filters[p - 1]
                        VGL11.glClear(VGL11.GL_COLOR_BUFFER_BIT or VGL11.GL_DEPTH_BUFFER_BIT)
                        filter.gpuShader.render(filter.uniforms + contextUniforms, filterScreenQuad)
                    }
                }
                context["filterColorTexture"] = frameBuffer?.colorTexture
                context["filterDepthTexture"] = frameBuffer?.depthTexture
            }
        }
    }

    fun renderTo(fb: GpuFrameBuffer?, block: () -> Unit) {
        if (fb == null)
            block()
        else
            fb.exec { block() }
    }

    private fun renderShadowMap() {
        shadower.render(light)
        context["shadowTexture"] = shadower.texture
        context["shadowProjection"] = shadower.projection?.mat4()
        context["shadowView"] = shadower.camera?.mat4()
    }

    private fun updateFrameInfo(): FrameInfo {
        val now = nanoTime()
        val frameTime = now - prevFrameNano
        frames.add(frameTime)
        val frameInfo =
            FrameInfo(frameNumber, (now - startNanos) * 1e-9f, frameTime * 1e-9f, calcAverageFps())
        prevFrameNano = now
        frameNumber++
        return frameInfo
    }

    private fun render() {
        VGL11.glViewport(0, 0, width, height)
        VGL11.glEnable(VGL11.GL_CULL_FACE)
        VGL11.glCullFace(VGL11.GL_BACK)
        VGL11.glClear(VGL11.GL_COLOR_BUFFER_BIT or VGL11.GL_DEPTH_BUFFER_BIT)

        screens.forEach { it.render(contextUniforms) }
        renderBucket(opaques, -1.0)
        skies.forEach { it.render(contextUniforms) }
        renderBucket(transparents, 1.0)
    }

    private fun renderBucket(renderables: MutableList<Renderable>, sortFactor: Double) {
        val visibleRenderables = renderables.filter { isVisible(it) }
            .sortedBy { (camera.mat4() * it.worldBoundingBox!!.center()).z * sortFactor }
        visibleRenderables.forEach { it.render(contextUniforms) }
    }

    private fun isVisible(renderable: Renderable): Boolean {
        return if (renderable.worldBoundingBox == null) {
            true
        } else {
            renderable.worldBoundingBox!!.isIn(projection.mat4() * camera.mat4())
        }
    }

    private fun calcAverageFps(): Float {
        while (frames.size > 128) {
            frames.poll()
        }
        return 1e9f / frames.average().toFloat()
    }

    private fun updateContext() {
        context["view"] = camera.mat4()
        context["projection"] = projection.mat4()
        context["cameraPos"] = camera.position()
        context["light"] = light
        context["screenWidth"] = width.toFloat()
        context["screenHeight"] = height.toFloat()
        context["time"] = (nanoTime() - startNanos) * 1e-9f
    }

    fun addFilter(filter: Filter) {
        filters.add(filter)
        if (filters.size == 1 || filters.size == 2) {
            filterFrameBuffers.add(gpu.createFrameBuffer(width, height, true))
        }
        // TODO: resizing screensize framebuffers ?
    }

}