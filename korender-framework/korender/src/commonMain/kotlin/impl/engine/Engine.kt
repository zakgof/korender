package com.zakgof.korender.impl.engine

import com.zakgof.korender.KorenderContext
import com.zakgof.korender.SceneDeclaration
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.camera.Camera
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.declaration.SceneContext
import com.zakgof.korender.declaration.Textures.texture
import com.zakgof.korender.impl.glgpu.GlGpu
import com.zakgof.korender.input.TouchEvent
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import com.zakgof.korender.projection.FrustumProjection
import com.zakgof.korender.projection.Projection
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.Predicate

internal class Engine(private var height: Int, private var width: Int, block: KorenderContext.() -> Unit) {

    private val touchQueue = ConcurrentLinkedQueue<TouchEvent>()
    private val sceneBlocks = mutableListOf<SceneContext.() -> Unit>() // TODO: ref
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
        val korenderContext = KorenderContext(sceneBlocks, touchHandlers)
        block.invoke(korenderContext)
    }

    fun frame() {
        val frameInfo = frameInfoManager.frame()

        processTouches()

        val sd = SceneDeclaration()
        projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f) // TODO
        sceneBlocks.forEach {
            val sc = SceneContext(frameInfo, sd, width, height, projection, camera, light).apply(it)
            projection = sc.projection
            camera = sc.camera
            light = sc.light
        }
        updateContext()
        inventory.go {
            val scene = Scene(sd, inventory, camera, width, height)
            scene.render(context, light)
            sceneTouchBoxesHandler = scene.touchBoxesHandler
        }
    }

    private fun updateContext() {
        context["noiseTexture"] = texture("/noise.png")
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
        width = w
        height = h
    }
}