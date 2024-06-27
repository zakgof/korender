package com.zakgof.korender.impl.engine

import com.zakgof.korender.KorenderException
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.camera.Camera
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.declaration.FrameContext
import com.zakgof.korender.declaration.KorenderContext
import com.zakgof.korender.declaration.Textures.texture
import com.zakgof.korender.impl.gl.VGL11
import com.zakgof.korender.impl.glgpu.GlGpu
import com.zakgof.korender.input.TouchEvent
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import com.zakgof.korender.projection.FrustumProjection
import com.zakgof.korender.projection.Projection
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.Predicate

internal class Engine(private var width: Int, private var height: Int, block: KorenderContext.() -> Unit) {

    private val touchQueue = ConcurrentLinkedQueue<TouchEvent>()
    private val frameBlocks = mutableListOf<FrameContext.() -> Unit>()
    private val inventory = Inventory(GlGpu())
    private val frameInfoManager = FrameInfoManager(inventory)

    private var camera: Camera = DefaultCamera(20.z, -1.z, 1.y)
    private var projection: Projection =
        FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    private var light = Vec3(1f, -1f, 0f).normalize()

    private val context = mutableMapOf<String, Any?>()
    private lateinit var sceneTouchBoxesHandler: Predicate<TouchEvent>
    private val touchHandlers = mutableListOf<TouchHandler>()

    init {
        println("Engine:init $width:$height")
        block.invoke(object : KorenderContext {
            override fun Frame(block: FrameContext.() -> Unit) {
                frameBlocks.add(block)
            }
            override fun OnTouch(handler: (TouchEvent) -> Unit) {
                touchHandlers.add(handler)
            }
        })
    }

    fun frame() {
        val frameInfo = frameInfoManager.frame()

        processTouches()

        val sd = SceneDeclaration()
        projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f) // TODO
        frameBlocks.forEach {
            val frameBlock = DefaultFrameContext(sd, frameInfo, width, height, projection, camera, light).apply(it)
            projection = frameBlock.projection
            camera = frameBlock.camera
            light = frameBlock.light
        }
        updateContext()
        inventory.go {
            val scene = Scene(sd, inventory, camera, width, height)
            scene.render(context, projection, camera, light)
            val error = VGL11.glGetError()
            if (error != 0) {
                throw KorenderException("Frame error $error")
            }
            sceneTouchBoxesHandler = scene.touchBoxesHandler
        }
    }

    private fun updateContext() {
        context["noiseTexture"] = texture("/noise.png")
        context["fbmTexture"] = texture("/fbm.png")
        context["view"] = camera.mat4
        context["projection"] = projection.mat4
        context["cameraPos"] = camera.position
        context["light"] = light
        context["screenWidth"] = width.toFloat()
        context["screenHeight"] = height.toFloat()
        context["time"] = (System.nanoTime() - frameInfoManager.startNanos) * 1e-9f
    }

    fun pushTouch(touchEvent: TouchEvent) = touchQueue.add(touchEvent)

    private fun processTouches() {
        do {
            val event = touchQueue.poll()
            event?.let { touchEvent ->
                if (!sceneTouchBoxesHandler.test(touchEvent)) {
                    touchHandlers.forEach { it(touchEvent) }
                }

            }
        } while (event != null)
    }

    fun resize(w: Int, h: Int) {
        println("Engine:resize $w:$h")
        width = w
        height = h
    }
}