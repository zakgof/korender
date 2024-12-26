package com.zakgof.korender.impl.engine

import com.zakgof.korender.AsyncContext
import com.zakgof.korender.KorenderException
import com.zakgof.korender.Platform
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.camera.Camera
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.gl.GL.glGetError
import com.zakgof.korender.impl.material.ResourceTextureDeclaration
import com.zakgof.korender.input.TouchEvent
import com.zakgof.korender.material.TextureDeclaration
import com.zakgof.korender.material.TextureFilter
import com.zakgof.korender.material.TextureWrap
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import com.zakgof.korender.projection.FrustumProjection
import com.zakgof.korender.projection.Projection
import kotlinx.coroutines.channels.Channel

internal class Engine(
    private var width: Int,
    private var height: Int,
    asyncContext: AsyncContext,
    block: KorenderContext.() -> Unit
) {

    private val touchQueue = Channel<TouchEvent>(Channel.UNLIMITED)
    private val frameBlocks = mutableListOf<FrameContext.() -> Unit>()
    private val inventory = Inventory(asyncContext)
    private val frameInfoManager = FrameInfoManager(inventory)

    private var camera: Camera = DefaultCamera(20.z, -1.z, 1.y)
    private var projection: Projection =
        FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    private var light = Vec3(1f, -1f, 0f).normalize()

    private val context = mutableMapOf<String, Any?>()
    private lateinit var sceneTouchBoxesHandler: (TouchEvent) -> Boolean
    private val touchHandlers = mutableListOf<TouchHandler>()

    init {
        println("Engine init $width x $height")
        block.invoke(object : KorenderContext {
            override fun Frame(block: FrameContext.() -> Unit) {
                frameBlocks.add(block)
            }

            override fun OnTouch(handler: (TouchEvent) -> Unit) {
                touchHandlers.add(handler)
            }

            override fun texture(
                textureResource: String,
                filter: TextureFilter,
                wrap: TextureWrap,
                aniso: Int
            ): TextureDeclaration = ResourceTextureDeclaration(textureResource, filter, wrap, aniso)

        })
    }

    fun frame() {
        val frameInfo = frameInfoManager.frame()
        processTouches()
        val sd = SceneDeclaration()
        projection = FrustumProjection(
            width = 5f * width / height,
            height = 5f,
            near = 10f,
            far = 1000f
        ) // TODO
        frameBlocks.forEach {
            val frameBlock =
                DefaultFrameContext(sd, frameInfo, width, height, projection, camera, light).apply(
                    it
                )
            projection = frameBlock.projection
            camera = frameBlock.camera
            light = frameBlock.light
        }
        updateContext()
        inventory.go {
            val scene = Scene(sd, inventory, camera, width, height, frameInfo.time)
            scene.render(context, projection, camera, light)
            val error = glGetError()
            if (error != 0) {
                throw KorenderException("Frame error $error")
            }
            sceneTouchBoxesHandler = scene.touchBoxesHandler
        }
    }

    private fun updateContext() {
        context["noiseTexture"] = ResourceTextureDeclaration("noise.png")
        context["fbmTexture"] = ResourceTextureDeclaration("fbm.png")
        context["view"] = camera.mat4
        context["projection"] = projection.mat4
        context["cameraPos"] = camera.position
        context["light"] = light
        context["screenWidth"] = width.toFloat()
        context["screenHeight"] = height.toFloat()
        context["time"] = (Platform.nanoTime() - frameInfoManager.startNanos) * 1e-9f
    }

    suspend fun pushTouch(touchEvent: TouchEvent) = touchQueue.send(touchEvent)

    private fun processTouches() {
        do {
            val event = touchQueue.tryReceive().getOrNull()
            event?.let { touchEvent ->
                if (!sceneTouchBoxesHandler(touchEvent)) {
                    touchHandlers.forEach { it(touchEvent) }
                }
            }
        } while (event != null)
    }

    fun resize(w: Int, h: Int) {
        println("Engine resize $w x $h")
        width = w
        height = h
    }
}