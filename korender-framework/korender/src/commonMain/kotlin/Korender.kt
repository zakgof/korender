package com.zakgof.korender

import androidx.compose.runtime.Composable
import com.zakgof.korender.camera.Camera
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.declaration.MaterialDeclaration
import com.zakgof.korender.declaration.MeshDeclaration
import com.zakgof.korender.declaration.ShaderDeclaration
import com.zakgof.korender.geometry.Mesh
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.gl.VGL11
import com.zakgof.korender.glgpu.GlGpu
import com.zakgof.korender.gpu.Gpu
import com.zakgof.korender.gpu.GpuFrameBuffer
import com.zakgof.korender.gpu.GpuMesh
import com.zakgof.korender.gpu.GpuShader
import com.zakgof.korender.material.MapUniformSupplier
import com.zakgof.korender.material.Shaders
import com.zakgof.korender.material.UniformSupplier
import com.zakgof.korender.math.Transform
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

    private val filters = mutableListOf<Filter>()
    private val filterFrameBuffers = mutableListOf<GpuFrameBuffer>()

    private val context = mutableMapOf<String, Any?>()
    private val contextUniforms = MapUniformSupplier(context)

    private var frameNumber = 0L
    private var startNanos: Long = nanoTime()
    private var prevFrameNano: Long = nanoTime()
    private val frames: Queue<Long> = LinkedList()

    private val gpu: Gpu = GlGpu()
    private val inventory = Inventory(gpu)
    private val filterScreenQuad: GpuMesh = Meshes.screenQuad().build(gpu).gpuMesh

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

    fun resize(w: Int, h: Int) {
        if (width != w || height != h) {
            width = w
            height = h
            val bufferCount = filterFrameBuffers.size
            filterFrameBuffers.forEach { it.close() }
            filterFrameBuffers.clear()
            for (i in 0 until bufferCount) {
                filterFrameBuffers.add(gpu.createFrameBuffer(width, height, false))
            }
        }
        onResize.invoke(this)
    }

    fun frame() {
        val frameInfo = updateFrameInfo()
        updateContext()

        val scene = processScene(frameInfo)

        onFrame.invoke(this, frameInfo)
        renderShadowMap()

        if (filters.isEmpty()) {
            render(scene)
        } else {
            for (p in 0..filters.size) {
                val frameBuffer = if (p == filters.size) null else filterFrameBuffers.get(p % 2)
                renderTo(frameBuffer) {
                    if (p == 0) {
                        VGL11.glClear(VGL11.GL_COLOR_BUFFER_BIT or VGL11.GL_DEPTH_BUFFER_BIT)
                        render(scene)
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

    private fun render(scene: Scene) {
        VGL11.glViewport(0, 0, width, height)
        VGL11.glEnable(VGL11.GL_CULL_FACE)
        VGL11.glCullFace(VGL11.GL_BACK)
        VGL11.glClear(VGL11.GL_COLOR_BUFFER_BIT or VGL11.GL_DEPTH_BUFFER_BIT)
        scene.render(camera, contextUniforms)
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
            filterFrameBuffers.add(gpu.createFrameBuffer(width, height, false))
        }
        // TODO: resizing screensize framebuffers ?
    }

    fun Scene(block: SceneContext.() -> Unit) {
        if (sceneBlock != null) {
            throw KorenderException("Only one Scene is allowed")
        }
        sceneBlock = block;
    }


    private fun processScene(frameInfo: FrameInfo): Scene {
        val sd = SceneDeclaration()
        sceneBlock?.invoke(SceneContext(frameInfo, sd))
        return Scene(sd, inventory)
    }

    class SceneContext(val frameInfo: FrameInfo, private val sceneBuilder: SceneDeclaration) {
        fun Renderable(
            mesh: MeshDeclaration,
            material: MaterialDeclaration,
            transform: Transform
        ) =
            sceneBuilder.add(
                RenderableDeclaration(
                    mesh,
                    material.shader,
                    material.uniforms,
                    transform
                )
            )
    }

}

class SceneDeclaration {

    val renderables = mutableListOf<RenderableDeclaration>()
    fun add(renderable: RenderableDeclaration) {
        renderables.add(renderable)
    }
}

class Scene(decl: SceneDeclaration, inventory: Inventory) {

    private val opaques = mutableListOf<Renderable>()
    private val transparents = mutableListOf<Renderable>()
    private val skies = mutableListOf<Renderable>()
    private val screens = mutableListOf<Renderable>()

    init {
        inventory.go {
            decl.renderables.forEach {
                val renderable = create(this, it)
                when (it.bucket) {
                    Bucket.OPAQUE -> opaques.add(renderable)
                    Bucket.TRANSPARENT -> transparents.add(renderable)
                    Bucket.SKY -> skies.add(renderable)
                    Bucket.SCREEN -> screens.add(renderable)
                }
            }
        }
    }

    private fun create(inventory: Inventory, declaration: RenderableDeclaration): Renderable {
        val mesh = inventory.mesh(declaration.mesh)
        val shader = inventory.shader(declaration.shader)
        val uniforms = declaration.uniforms
        val transform = declaration.transform
        return Renderable(mesh, shader, uniforms, transform)
    }

    fun render(camera: Camera, contextUniforms: UniformSupplier) {
        screens.forEach { it.render(contextUniforms) }
        renderBucket(opaques, -1.0, camera, contextUniforms)
        skies.forEach { it.render(contextUniforms) }
        renderBucket(transparents, 1.0, camera, contextUniforms)
    }

    private fun renderBucket(
        renderables: MutableList<Renderable>,
        sortFactor: Double,
        camera: Camera,
        contextUniforms: UniformSupplier
    ) {
//        val visibleRenderables = renderables.filter { isVisible(it) }
//            .sortedBy {
//                val worldBB = it.mesh.modelBoundingBox!!.transform(it.transform)
//                (camera.mat4() * worldBB.center()).z * sortFactor
//            }
//        visibleRenderables.forEach { it.render(contextUniforms) }
        // TODO
        renderables.forEach { it.render(contextUniforms) }
    }

//    private fun isVisible(renderable: Renderable): Boolean {
//        return if (renderable.worldBoundingBox == null) {
//            true
//        } else {
//            renderable.worldBoundingBox!!.isIn(projection.mat4() * camera.mat4())
//        }
//    }


}

class Registry<D, R : AutoCloseable>(private val factory: (D) -> R) {

    private val map = mutableMapOf<D, R>()
    private var unusedKeys = mutableSetOf<D>()

    fun begin() {
        unusedKeys = HashSet(map.keys)
    }

    fun end() {
        unusedKeys.forEach { map[it]!!.close() }
    }

    operator fun get(decl: D): R {
        unusedKeys.remove(decl)
        return map.computeIfAbsent(decl) { factory(it) }
    }

}

class Inventory(private val gpu: Gpu) {

    private val meshes = Registry<MeshDeclaration, Mesh> { Meshes.create(it, gpu) }
    private val shaders = Registry<ShaderDeclaration, GpuShader> { Shaders.create(it, gpu) }

    fun go(block: Inventory.() -> Unit) {
        meshes.begin()
        shaders.begin()
        block.invoke(this)
        meshes.end()
        shaders.end()
    }

    fun mesh(decl: MeshDeclaration): Mesh = meshes[decl]

    fun shader(decl: ShaderDeclaration): GpuShader = shaders[decl]

}

class RenderableDeclaration(
    val mesh: MeshDeclaration,
    val shader: ShaderDeclaration,
    val uniforms: UniformSupplier,
    val transform: Transform,
    val bucket: Bucket = Bucket.OPAQUE
)