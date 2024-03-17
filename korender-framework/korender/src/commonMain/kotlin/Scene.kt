package com.zakgof.korender

import com.zakgof.korender.camera.Camera
import com.zakgof.korender.declaration.FilterDeclaration
import com.zakgof.korender.declaration.InstancedBillboardsContext
import com.zakgof.korender.declaration.MeshDeclaration
import com.zakgof.korender.declaration.ShaderDeclaration
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.gl.VGL11
import com.zakgof.korender.gpu.GpuFrameBuffer
import com.zakgof.korender.gpu.GpuMesh
import com.zakgof.korender.material.MapUniformSupplier
import com.zakgof.korender.material.UniformSupplier

class Scene(decl: SceneDeclaration, inventory: Inventory) {

    private lateinit var filters: List<Filter>

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
            filters = decl.filters.map {
                create(this, it)
            }
        }
    }

    private fun create(inventory: Inventory, declaration: RenderableDeclaration): Renderable {
        val mesh = inventory.mesh(declaration.mesh)

        if (declaration.mesh is MeshDeclaration.InstancedBillboardDeclaration) {
            val instances = InstancedBillboardsContext().apply(declaration.mesh.block).instances
            (mesh as Meshes.DefaultMesh).updateBillboardInstances(instances)
        }

        val shader = inventory.shader(declaration.shader)
        declaration.uniforms.boil {
            inventory.texture(it as String)
        }
        val uniforms = declaration.uniforms
        val transform = declaration.transform
        return Renderable(mesh, shader, uniforms, transform)
    }

    private fun create(inventory: Inventory, declaration: FilterDeclaration): Filter {
        val shader =
            inventory.shader(ShaderDeclaration("screen.vert", declaration.fragment, setOf()))
        return Filter(shader, declaration.uniforms)
    }

    fun renderAll(
        camera: Camera,
        contextUniforms: UniformSupplier,
        filterFrameBuffers: List<GpuFrameBuffer>,
        filterScreenQuad: GpuMesh
    ) {
        if (filters.isEmpty()) {
            render(camera, contextUniforms)
        } else {
            val prevFrameContext = mutableMapOf<String, Any?>()
            for (p in 0..filters.size) {
                val frameBuffer = if (p == filters.size) null else filterFrameBuffers[p % 2]
                renderTo(frameBuffer) {
                    if (p == 0) {
                        VGL11.glClear(VGL11.GL_COLOR_BUFFER_BIT or VGL11.GL_DEPTH_BUFFER_BIT)
                        render(camera, contextUniforms)
                    } else {
                        val filter = filters[p - 1]
                        VGL11.glClear(VGL11.GL_COLOR_BUFFER_BIT or VGL11.GL_DEPTH_BUFFER_BIT)
                        filter.gpuShader.render(
                            filter.uniforms + contextUniforms + MapUniformSupplier(
                                prevFrameContext
                            ), filterScreenQuad
                        )
                    }
                }
                prevFrameContext["filterColorTexture"] = frameBuffer?.colorTexture
                prevFrameContext["filterDepthTexture"] = frameBuffer?.depthTexture
            }
        }
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


    private fun renderTo(fb: GpuFrameBuffer?, block: () -> Unit) {
        if (fb == null) block()
        else fb.exec { block() }
    }

}