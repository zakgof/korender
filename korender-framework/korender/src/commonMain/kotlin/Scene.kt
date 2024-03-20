package com.zakgof.korender

import com.zakgof.korender.camera.Camera
import com.zakgof.korender.declaration.Direction
import com.zakgof.korender.declaration.ElementDeclaration
import com.zakgof.korender.declaration.FilterDeclaration
import com.zakgof.korender.declaration.InstancedBillboardsContext
import com.zakgof.korender.declaration.InstancedRenderablesContext
import com.zakgof.korender.declaration.MeshDeclaration
import com.zakgof.korender.declaration.RenderableDeclaration
import com.zakgof.korender.declaration.ShaderDeclaration
import com.zakgof.korender.font.Fonts
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.gl.VGL11
import com.zakgof.korender.gpu.GpuFrameBuffer
import com.zakgof.korender.gpu.GpuMesh
import com.zakgof.korender.material.MapUniformSupplier
import com.zakgof.korender.material.Shaders
import com.zakgof.korender.material.UniformSupplier
import com.zakgof.korender.math.Vec2
import kotlin.math.max

class Scene(
    sceneDeclaration: SceneDeclaration,
    inventory: Inventory,
    private val camera: Camera,
    private val width: Int,
    private val height: Int
) {

    private lateinit var filters: List<Filter>

    private val opaques = mutableListOf<Renderable>()
    private val transparents = mutableListOf<Renderable>()
    private val skies = mutableListOf<Renderable>()
    private val screens = mutableListOf<Renderable>()

    init {
        inventory.go {
            sceneDeclaration.renderables.forEach {
                val renderable = create(this, it)
                when (it.bucket) {
                    Bucket.OPAQUE -> opaques.add(renderable)
                    Bucket.TRANSPARENT -> transparents.add(renderable)
                    Bucket.SKY -> skies.add(renderable)
                    Bucket.SCREEN -> screens.add(renderable)
                }
            }
            filters = sceneDeclaration.filters.map { create(this, it) }
            sceneDeclaration.gui?.let { layoutGui(this, 0, 0, width, height, it) }
        }
    }

    private fun layoutGui(
        inventory: Inventory,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        container: ElementDeclaration.ContainerDeclaration
    ) {
        val sizes = mutableMapOf<ElementDeclaration, Size>()
        sizeEm(Direction.Vertical, container, sizes, inventory)
        layoutContainer(sizes, inventory, x, y, width, height, container)
    }

    private fun layoutContainer(
        sizes: MutableMap<ElementDeclaration, Size>,
        inventory: Inventory,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        container: ElementDeclaration.ContainerDeclaration
    ) {
        if (container.direction == Direction.Vertical) {
            val fillers = container.elements.count { sizes[it]!!.height < 0 }
            val normalsHeight =
                container.elements.map { sizes[it]!!.height }.filter { it >= 0 }.sum()
            val fillerHeight = if (fillers == 0) 0 else (height - normalsHeight) / fillers
            var currY = y
            for (child in container.elements) {
                val declSize = sizes[child]!!
                val childWidth = if (declSize.width < 0) width else declSize.width
                val childHeight = if (declSize.height < 0) fillerHeight else declSize.height
                when (child) {
                    is ElementDeclaration.TextDeclaration -> createText(child, inventory, x, currY, childWidth, childHeight)
                    is ElementDeclaration.ImageDeclaration -> createImage(child, inventory, x, currY)
                    is ElementDeclaration.ContainerDeclaration ->
                        layoutContainer(sizes, inventory, x, currY, childWidth, childHeight, child)
                    is ElementDeclaration.FillerDeclaration -> {}
                }
                currY += childHeight
            }
        }
        if (container.direction == Direction.Horizontal) {
            val fillers = container.elements.count { sizes[it]!!.width < 0 }
            val normalsWidths =
                container.elements.map { sizes[it]!!.width }.filter { it >= 0 }.sum()
            val fillerWidth = if (fillers == 0) 0 else (width - normalsWidths) / fillers
            var currX = x
            for (child in container.elements) {
                val declSize = sizes[child]!!
                val childWidth = if (declSize.width < 0) fillerWidth else declSize.width
                val childHeight = if (declSize.height < 0) height else declSize.height
                when (child) {
                    is ElementDeclaration.TextDeclaration -> createText(child, inventory, currX, y, childWidth, childHeight)
                    is ElementDeclaration.ImageDeclaration -> createImage(child, inventory, currX, y)
                    is ElementDeclaration.ContainerDeclaration ->
                        layoutContainer(sizes, inventory, x, currX, y, childHeight, child)
                    is ElementDeclaration.FillerDeclaration -> {
                    }
                }
                currX += childWidth
            }
        }
    }

    private fun createImage(declaration: ElementDeclaration.ImageDeclaration, inventory: Inventory, x: Int, y: Int) {
        screens.add(Renderable(
            mesh = inventory.mesh(MeshDeclaration.ImageQuadDeclaration),
            shader = inventory.shader(Shaders.imageQuadDeclaration),
            uniforms = MapUniformSupplier(
                Pair("pos", Vec2(x.toFloat() / width, 1.0f - (y.toFloat() + declaration.height.toFloat()) / height)),
                Pair("size", Vec2(declaration.width.toFloat() / width, declaration.height.toFloat() / height)),
                Pair("imageTexture", inventory.texture(declaration.imageResource))
            )
        ))
    }

    private fun createText(declaration: ElementDeclaration.TextDeclaration, inventory: Inventory, x: Int, y: Int, w: Int, h: Int) {
        val mesh = inventory.fontMesh(declaration.id)
        val font = inventory.font(declaration.fontResource)
        mesh.updateFont(
            declaration.text,
            declaration.height.toFloat() / height,
            height.toFloat()/width.toFloat(),
            x.toFloat() / width,
            1.0f - y.toFloat() / height,
            font.widths
        )
        screens.add(Renderable(
            mesh = mesh,
            shader = inventory.shader(Fonts.shaderDeclaration),
            uniforms = MapUniformSupplier(mapOf(
                Pair("color", declaration.color),
                Pair("fontTexture", font.gpuTexture)
            ))
        ))
    }

    private fun sizeEm(
        parentDirection: Direction,
        element: ElementDeclaration,
        sizes: MutableMap<ElementDeclaration, Size>,
        inventory: Inventory
    ): Scene.Size {
        val size = when (element) {
            is ElementDeclaration.TextDeclaration -> textSize(element, inventory)
            is ElementDeclaration.ImageDeclaration -> Size(element.width, element.height)
            is ElementDeclaration.FillerDeclaration -> {
                if (parentDirection == Direction.Vertical) Size(0, -1) else Size(-1, 0)
            }
            is ElementDeclaration.ContainerDeclaration -> {
                if (element.direction == Direction.Vertical) {
                    var w = 0
                    var h = 0
                    for (child in element.elements) {
                        val childSize = sizeEm(element.direction, child, sizes, inventory)
                        if (w >= 0) {
                            w = if (childSize.width < 0) -1 else max(w, childSize.width)
                        }
                        if (h >= 0) {
                            if (childSize.height < 0) {
                                h = -1
                            } else {
                                h += childSize.height
                            }
                        }
                    }
                    Size(w, h)
                } else {
                    var w = 0
                    var h = 0
                    for (child in element.elements) {
                        val childSize = sizeEm(element.direction, child, sizes, inventory)
                        if (h >= 0) {
                            h = if (childSize.height < 0) -1 else max(h, childSize.height)
                        }
                        if (w >= 0) {
                            if (childSize.width < 0) {
                                w = -1
                            } else {
                                w += childSize.width
                            }
                        }
                    }
                    Size(w, h)
                }
            }
        }
        sizes.put(element, size)
        return size
    }

    private fun textSize(
        textDeclaration: ElementDeclaration.TextDeclaration,
        inventory: Inventory
    ): Size {
        val font = inventory.font(textDeclaration.fontResource)
        return Size(
            font.textWidth(textDeclaration.height, textDeclaration.text),
            textDeclaration.height
        )
    }

    class Size(val width: Int, val height: Int)

    private fun create(inventory: Inventory, declaration: RenderableDeclaration): Renderable {

        val new = !inventory.hasMesh(declaration.mesh)
        val mesh = inventory.mesh(declaration.mesh)

        if (declaration.mesh is MeshDeclaration.InstancedBillboardDeclaration) {
            val instances = InstancedBillboardsContext().apply(declaration.mesh.block).instances
            if (declaration.mesh.zSort) {
                instances.sortBy { (camera.mat4() * it.pos).z }
            }
            (mesh as Meshes.InstancedMesh).updateBillboardInstances(instances)
        }

        if (declaration.mesh is MeshDeclaration.InstancedRenderableDeclaration) {
            if (!declaration.mesh.static || new) {
                val instances =
                    InstancedRenderablesContext().apply(declaration.mesh.block).instances
                (mesh as Meshes.InstancedMesh).updateInstances(instances)
            }
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