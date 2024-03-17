package com.zakgof.korender

import androidx.compose.runtime.Composable
import com.zakgof.korender.camera.Camera
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.declaration.FilterDeclaration
import com.zakgof.korender.declaration.MeshDeclaration
import com.zakgof.korender.declaration.RenderableDeclaration
import com.zakgof.korender.declaration.ShaderDeclaration
import com.zakgof.korender.geometry.Mesh
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.gl.VGL11
import com.zakgof.korender.glgpu.GlGpu
import com.zakgof.korender.gpu.Gpu
import com.zakgof.korender.gpu.GpuFrameBuffer
import com.zakgof.korender.gpu.GpuMesh
import com.zakgof.korender.gpu.GpuShader
import com.zakgof.korender.gpu.GpuTexture
import com.zakgof.korender.material.MapUniformSupplier
import com.zakgof.korender.material.Shaders
import com.zakgof.korender.material.Textures
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
import kotlin.math.min

@Composable
fun Korender(block: KorenderContext.() -> Unit) {

    lateinit var korender: KorenderContext

    getPlatform().openGL(
        init = { w, h ->
            VGL11.glEnable(VGL11.GL_BLEND)
            VGL11.glEnable(VGL11.GL_DEPTH_TEST)
            VGL11.glBlendFunc(VGL11.GL_SRC_ALPHA, VGL11.GL_ONE_MINUS_SRC_ALPHA)
            VGL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
            korender = KorenderContext(w, h)
            block.invoke(korender)
            korender.onResize.invoke(korender)
        },
        frame = { korender.frame() },
        resize = { w, h -> korender.resize(w, h) }
    )
}

class KorenderContext(var width: Int, var height: Int) {

    private var sceneBlock: (SceneContext.() -> Unit)? = null

    private val context = mutableMapOf<String, Any?>()
    private val contextUniforms = MapUniformSupplier(context)

    private var frameNumber = 0L
    private var startNanos: Long = nanoTime()
    private var prevFrameNano: Long = nanoTime()
    private val frames: Queue<Long> = LinkedList()

    private val gpu: Gpu = GlGpu()
    private val inventory = Inventory(gpu)

    var shadower: Shadower = SimpleShadower(gpu)
    var onFrame: KorenderContext.(FrameInfo) -> Unit = {}
    var onResize: KorenderContext.() -> Unit = {}
    var camera: Camera = DefaultCamera(20.z, -1.z, 1.y)
        set(value) {
            field = value
            updateContext()
        }
    var projection: Projection =
        FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
        set(value) {
            field = value
            updateContext()
        }
    var light = Vec3(1f, -1f, 0f).normalize()

    private val filters = mutableListOf<Filter>()
    private val filterFrameBuffers = mutableListOf<GpuFrameBuffer>()
    private val filterScreenQuad: GpuMesh = Meshes.screenQuad().build(inventory.gpu).gpuMesh

    fun resize(w: Int, h: Int) {
        if (w != width || h != height) {
            filterFrameBuffers.forEach { it.close() }
            filterFrameBuffers.clear()
            width = w
            height = h
        }
        onResize.invoke(this)
    }

    fun frame() {
        val frameInfo = updateFrameInfo()
        updateContext()

        val sd = SceneDeclaration()
        sceneBlock?.invoke(SceneContext(frameInfo, sd))

        updateFilterFramebuffers(sd.filters)
        val scene = Scene(sd, inventory, camera)

        onFrame.invoke(this, frameInfo)
        renderShadowMap()

        VGL11.glViewport(0, 0, width, height)
        VGL11.glEnable(VGL11.GL_CULL_FACE)
        VGL11.glCullFace(VGL11.GL_BACK)
        VGL11.glClear(VGL11.GL_COLOR_BUFFER_BIT or VGL11.GL_DEPTH_BUFFER_BIT)
        scene.renderAll(contextUniforms, filterFrameBuffers, filterScreenQuad)
    }

    private fun updateFilterFramebuffers(declarations: List<FilterDeclaration>) {
        val requiredSize = min(declarations.size, 2)
        while (filterFrameBuffers.size > requiredSize) {
            filterFrameBuffers.last().close()
            filterFrameBuffers.removeLast();
        }
        while (filterFrameBuffers.size < requiredSize) {
            filterFrameBuffers.add(gpu.createFrameBuffer(width, height, false))
        }
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

    fun Scene(block: SceneContext.() -> Unit) {
        if (sceneBlock != null) {
            throw KorenderException("Only one Scene is allowed")
        }
        sceneBlock = block;
    }

}

class SceneDeclaration {

    val renderables = mutableListOf<RenderableDeclaration>()
    val filters = mutableListOf<FilterDeclaration>()
    fun add(renderable: RenderableDeclaration) = renderables.add(renderable)
    fun add(filter: FilterDeclaration) = filters.add(filter)
}

class Registry<D, R : AutoCloseable>(private val factory: (D) -> R) {

    private val map = mutableMapOf<D, R>()
    private var unusedKeys = mutableSetOf<D>()

    fun begin() {
        unusedKeys = HashSet(map.keys)
    }

    fun end() {
        unusedKeys.forEach {
            map[it]!!.close()
            map.remove(it)
        }
    }

    operator fun get(decl: D): R {
        unusedKeys.remove(decl)
        return map.computeIfAbsent(decl) { factory(it) }
    }

}

class Inventory(val gpu: Gpu) {

    private val meshes = Registry<MeshDeclaration, Mesh> { Meshes.create(it, gpu) }
    private val shaders = Registry<ShaderDeclaration, GpuShader> { Shaders.create(it, gpu) }
    private val textures = Registry<String, GpuTexture> { Textures.create(it).build(gpu) }

    fun go(block: Inventory.() -> Unit) {
        meshes.begin()
        shaders.begin()
        textures.begin()
        block.invoke(this)
        meshes.end()
        shaders.end()
        textures.end()
    }

    fun mesh(decl: MeshDeclaration): Mesh = meshes[decl]

    fun shader(decl: ShaderDeclaration): GpuShader = shaders[decl]

    fun texture(decl: String): GpuTexture = textures[decl]

}