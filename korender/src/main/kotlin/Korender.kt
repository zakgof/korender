package com.zakgof.korender

import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.glgpu.GlGpu
import com.zakgof.korender.material.MapUniformSupplier
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.projection.OrthoProjection
import com.zakgof.korender.projection.Projection
import com.zakgof.korender.shadow.Shadower
import com.zakgof.korender.shadow.SimpleShadower
import gl.VGL11
import org.lwjgl.opengl.GL11
import java.lang.System.nanoTime
import java.util.*

fun korender(platform: Platform, block: KorenderContext.() -> Unit) {
    var korender: KorenderContext? = null
    platform.run(
        width = 1280,
        height = 800,
        init = {
            korender = KorenderContext()
            block.invoke(korender!!)
            korender!!.onResize.invoke(korender!!)
        },
        onFrame = { korender!!::frame },
        onResize = { x, y -> korender!!.resize(x, y) }
    )
}

class KorenderContext(var width: Int = 1280, var height: Int = 800) {

    private val renderables = mutableListOf<Renderable>()
    private val context = mutableMapOf<String, Any?>()
    private val contextUniforms = MapUniformSupplier(context)

    private var startNanos: Long = nanoTime()
    private var prevFrameNano: Long = nanoTime()
    private val frames: Queue<Long> = LinkedList()
    private var frameRenderableCount: Int = 0
    private var frameVisibleRenderableCount: Int = 0

    val gpu: Gpu = GlGpu()
    var shadower: Shadower = SimpleShadower(gpu)
    var onFrame: KorenderContext.(FrameInfo) -> Unit = {}
    var onResize: KorenderContext.() -> Unit = {}
    var camera: DefaultCamera = DefaultCamera(
        pos = Vec3(0f, 5f, 15f),
        dir = Vec3(0f, 0f, -1f),
        up = Vec3(0f, 1f, 0f)
    )
        set(value) {
            field = value
            updateContext()
        }
    var projection: Projection = OrthoProjection(
        width = 10f,
        height = 10f,
        near = 10f,
        far = 10000f
    )
        set(value) {
            field = value
            updateContext()
        }
    var light = Vec3(1f, -1f, 0f).normalize()

    fun add(renderable: Renderable) {
        renderables.add(renderable) // TODO standard buckets
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
        render()
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
            FrameInfo(now - startNanos, frameTime, calcAverageFps(), frameRenderableCount, frameVisibleRenderableCount)
        prevFrameNano = now
        return frameInfo
    }

    private fun render() {
        VGL11.glViewport(0, 0, width, height)
        VGL11.glEnable(GL11.GL_CULL_FACE)
        VGL11.glCullFace(VGL11.GL_BACK)

        val visibleRenderables = renderables.filter { isVisible(it) }
        frameRenderableCount = renderables.size
        frameVisibleRenderableCount = visibleRenderables.size
        visibleRenderables.forEach { it.render(contextUniforms) }
    }

    private fun isVisible(renderable: Renderable): Boolean {
        return if (renderable.worldBoundingBox == null) {
            true
        } else {
            renderable.worldBoundingBox!!.isIn(projection.mat4() * camera.mat4())
        }
    }

    private fun calcAverageFps(): Double {
        while (frames.size > 128) {
            frames.poll()
        }
        return 1e9 / frames.average()
    }

    private fun updateContext() {
        context["view"] = camera.mat4()
        context["projection"] = projection.mat4()
        context["cameraPos"] = camera.position()
        context["light"] = light
    }
}