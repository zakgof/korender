package com.zakgof.korender.impl.engine

import com.zakgof.korender.SceneDeclaration
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.camera.Camera
import com.zakgof.korender.declaration.Direction
import com.zakgof.korender.declaration.InstancedBillboardsContext
import com.zakgof.korender.declaration.InstancedRenderablesContext
import com.zakgof.korender.declaration.MeshDeclaration
import com.zakgof.korender.declaration.TextureDeclaration
import com.zakgof.korender.declaration.UniformSupplier
import com.zakgof.korender.impl.font.Fonts
import com.zakgof.korender.impl.geometry.Geometry
import com.zakgof.korender.impl.gpu.GpuFrameBuffer
import com.zakgof.korender.impl.material.MapUniformSupplier
import com.zakgof.korender.impl.material.Shaders
import com.zakgof.korender.input.TouchEvent
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import kotlin.math.max

internal class Scene(sceneDeclaration: SceneDeclaration, private val inventory: Inventory, private val camera: Camera, private val width: Int, private val height: Int) {

    private val shadower: SimpleShadower?
    private val filters: List<Filter>
    private val filterFrameBuffers: List<GpuFrameBuffer>

    private val opaques = mutableListOf<Renderable>()
    private val transparents = mutableListOf<Renderable>()
    private val skies = mutableListOf<Renderable>()
    private val screens = mutableListOf<Renderable>()

    private val touchBoxes = mutableListOf<TouchBox>();
    val touchHandler: (TouchEvent) -> Unit = { evt ->
        touchBoxes.forEach { it.touch(evt) }
    }

    init {
        shadower = sceneDeclaration.shadow?.let {
            val shadowCasters = mutableListOf<Renderable>()
            it.renderables.forEach { rd ->
                sceneDeclaration.renderables.add(rd)
                shadowCasters.add(createRenderable(rd))
            }
            SimpleShadower(inventory, it, shadowCasters)
        }
        sceneDeclaration.renderables.forEach {
            val renderable = createRenderable(it)
            when (it.bucket) {
                Bucket.OPAQUE -> opaques.add(renderable)
                Bucket.TRANSPARENT -> transparents.add(renderable)
                Bucket.SKY -> skies.add(renderable)
                Bucket.SCREEN -> screens.add(renderable)
            }
        }
        filters = sceneDeclaration.filters.map { createFilter(it) }
        filterFrameBuffers = filterFrameBuffers()
        sceneDeclaration.gui?.let { layoutGui(width, height, it) }
    }

    private fun layoutGui(width: Int, height: Int, container: ElementDeclaration.Container) {
        val sizes = mutableMapOf<ElementDeclaration, Size>()
        sizeEm(Direction.Vertical, container, sizes)
        layoutContainer(sizes, 0, 0, width, height, container)
    }

    private fun layoutContainer(sizes: MutableMap<ElementDeclaration, Size>, x: Int, y: Int, width: Int, height: Int, container: ElementDeclaration.Container) {
        if (container.direction == Direction.Vertical) {
            val fillers = container.elements.count { sizes[it]!!.height < 0 }
            val normalsHeight = container.elements.map { sizes[it]!!.height }.filter { it >= 0 }.sum()
            val fillerHeight = if (fillers == 0) 0 else (height - normalsHeight) / fillers
            var currY = y
            for (child in container.elements) {
                val declSize = sizes[child]!!
                val childWidth = if (declSize.width < 0) width else declSize.width
                val childHeight = if (declSize.height < 0) fillerHeight else declSize.height
                when (child) {
                    is ElementDeclaration.Text -> createText(child, x, currY, childWidth)
                    is ElementDeclaration.Image -> createImage(child, x, currY)
                    is ElementDeclaration.Container -> layoutContainer(sizes, x, currY, childWidth, childHeight, child)
                    is ElementDeclaration.Filler -> {}
                }
                currY += childHeight
            }
        }
        if (container.direction == Direction.Horizontal) {
            val fillers = container.elements.count { sizes[it]!!.width < 0 }
            val normalsWidths = container.elements.map { sizes[it]!!.width }.filter { it >= 0 }.sum()
            val fillerWidth = if (fillers == 0) 0 else (width - normalsWidths) / fillers
            var currX = x
            for (child in container.elements) {
                val declSize = sizes[child]!!
                val childWidth = if (declSize.width < 0) fillerWidth else declSize.width
                val childHeight = if (declSize.height < 0) height else declSize.height
                when (child) {
                    is ElementDeclaration.Text -> createText(child, currX, y, childWidth)
                    is ElementDeclaration.Image -> createImage(child, currX, y)
                    is ElementDeclaration.Container -> layoutContainer(sizes, currX, y, childWidth, childHeight, child)
                    is ElementDeclaration.Filler -> {
                    }
                }
                currX += childWidth
            }
        }
    }

    private fun createImage(declaration: ElementDeclaration.Image, x: Int, y: Int) {
        screens.add(
            Renderable(
                mesh = inventory.mesh(MeshDeclaration.ImageQuad), shader = inventory.shader(Shaders.imageQuadDeclaration), uniforms = MapUniformSupplier(
                    Pair("pos", Vec2(x.toFloat() / width, 1.0f - (y.toFloat() + declaration.height.toFloat()) / height)),
                    Pair("size", Vec2(declaration.width.toFloat() / width, declaration.height.toFloat() / height)),
                    Pair("imageTexture", inventory.texture(declaration.imageResource))
                )
            )
        )
        touchBoxes.add(TouchBox(x, y, declaration.width, declaration.height, declaration.onTouch))
    }

    private fun createText(declaration: ElementDeclaration.Text, x: Int, y: Int, w: Int) {
        val mesh = inventory.fontMesh(declaration.id)
        val font = inventory.font(declaration.fontResource)
        mesh.updateFont(
            declaration.text, declaration.height.toFloat() / height, height.toFloat() / width.toFloat(), x.toFloat() / width, 1.0f - y.toFloat() / height, font.widths
        )
        screens.add(
            Renderable(
                mesh = mesh, shader = inventory.shader(Fonts.shaderDeclaration), uniforms = MapUniformSupplier(
                    Pair("color", declaration.color), Pair("fontTexture", font.gpuTexture)
                )
            )
        )
        touchBoxes.add(TouchBox(x, y, w, declaration.height, declaration.onTouch))
    }

    private fun sizeEm(parentDirection: Direction, element: ElementDeclaration, sizes: MutableMap<ElementDeclaration, Size>): Size {
        val size = when (element) {
            is ElementDeclaration.Text -> textSize(element, inventory)
            is ElementDeclaration.Image -> Size(element.width, element.height)
            is ElementDeclaration.Filler -> {
                if (parentDirection == Direction.Vertical) Size(0, -1) else Size(-1, 0)
            }

            is ElementDeclaration.Container -> {
                if (element.direction == Direction.Vertical) {
                    var w = 0
                    var h = 0
                    for (child in element.elements) {
                        val childSize = sizeEm(element.direction, child, sizes)
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
                        val childSize = sizeEm(element.direction, child, sizes)
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
        textDeclaration: ElementDeclaration.Text, inventory: Inventory
    ): Size {
        val font = inventory.font(textDeclaration.fontResource)
        return Size(
            font.textWidth(textDeclaration.height, textDeclaration.text), textDeclaration.height
        )
    }

    class Size(val width: Int, val height: Int)

    private fun createRenderable(declaration: RenderableDeclaration): Renderable {

        val new = !inventory.hasMesh(declaration.mesh)
        val mesh = inventory.mesh(declaration.mesh)

        if (declaration.mesh is MeshDeclaration.Custom && !declaration.mesh.static) {
            (mesh as Geometry.DefaultMesh).updateMesh(declaration.mesh.block)
        }

        if (declaration.mesh is MeshDeclaration.InstancedBillboard) {
            // TODO: static
            val instances = mutableListOf<BillboardInstance>();
            InstancedBillboardsContext(instances).apply(declaration.mesh.block)
            if (declaration.mesh.zSort) {
                instances.sortBy { (camera.mat4 * it.pos).z }
            }
            (mesh as Geometry.InstancedMesh).updateBillboardInstances(instances)
        }

        if (declaration.mesh is MeshDeclaration.InstancedMesh) {
            if (!declaration.mesh.static || new) {
                val instances = mutableListOf<MeshInstance>()
                InstancedRenderablesContext(instances).apply(declaration.mesh.block)
                (mesh as Geometry.InstancedMesh).updateInstances(instances)
            }
        }

        val shader = inventory.shader(declaration.shader)
        val uniforms = declaration.uniforms
        val transform = declaration.transform
        return Renderable(mesh, shader, uniforms, transform)
    }

    private fun createFilter(declaration: FilterDeclaration): Filter {
        val shader = inventory.shader(ShaderDeclaration("screen.vert", declaration.fragment, setOf()))
        return Filter(shader, declaration.uniforms)
    }

    fun render(context: MutableMap<String, Any?>, light: Vec3) {

        shadower?.let {
            context.putAll(it.render(light))
        }

        if (filters.isEmpty()) {
            render(camera, context)
        } else {
            val prevFrameContext = mutableMapOf<String, Any?>()
            for (p in 0..filters.size) {
                val frameBuffer = if (p == filters.size) null else filterFrameBuffers[p % 2]
                renderTo(frameBuffer) {
                    if (p == 0) {
                        com.zakgof.korender.impl.gl.VGL11.glClear(com.zakgof.korender.impl.gl.VGL11.GL_COLOR_BUFFER_BIT or com.zakgof.korender.impl.gl.VGL11.GL_DEPTH_BUFFER_BIT)
                        render(camera, context)
                    } else {
                        val filter = filters[p - 1]
                        com.zakgof.korender.impl.gl.VGL11.glClear(com.zakgof.korender.impl.gl.VGL11.GL_COLOR_BUFFER_BIT or com.zakgof.korender.impl.gl.VGL11.GL_DEPTH_BUFFER_BIT)
                        filter.gpuShader.render(
                            filter.uniforms + context + prevFrameContext,
                            inventory.mesh(MeshDeclaration.ScreenQuad).gpuMesh
                        )
                    }
                }
                prevFrameContext["filterColorTexture"] = frameBuffer?.colorTexture
                prevFrameContext["filterDepthTexture"] = frameBuffer?.depthTexture
            }
        }
    }

    private fun filterFrameBuffers(): MutableList<GpuFrameBuffer> {
        val filterFrameBuffers = mutableListOf<GpuFrameBuffer>()
        if (filters.isNotEmpty()) {
            filterFrameBuffers.add(inventory.frameBuffer(FrameBufferDeclaration("filter1", width, height, true)))
        }
        if (filters.size >= 2) {
            filterFrameBuffers.add(inventory.frameBuffer(FrameBufferDeclaration("filter2", width, height, true)))
        }
        return filterFrameBuffers
    }

    private fun render(camera: Camera, context: Map<String, Any?>) {

        com.zakgof.korender.impl.gl.VGL11.glViewport(0, 0, width, height)
        com.zakgof.korender.impl.gl.VGL11.glEnable(com.zakgof.korender.impl.gl.VGL11.GL_CULL_FACE)
        com.zakgof.korender.impl.gl.VGL11.glCullFace(com.zakgof.korender.impl.gl.VGL11.GL_BACK)
        com.zakgof.korender.impl.gl.VGL11.glClear(com.zakgof.korender.impl.gl.VGL11.GL_COLOR_BUFFER_BIT or com.zakgof.korender.impl.gl.VGL11.GL_DEPTH_BUFFER_BIT)

        val uniformDecorator: (UniformSupplier) -> UniformSupplier = {
            UniformSupplier { key ->
                var value = it[key] ?: context[key]
                if (value is TextureDeclaration) {
                    value = inventory.texture(value.textureResource)
                }
                value
            }
        }

        renderBucket(opaques, -1.0, camera, uniformDecorator)
        skies.forEach { it.render(uniformDecorator) }
        renderBucket(transparents, 1.0, camera, uniformDecorator)
        screens.forEach { it.render(uniformDecorator) } // TODO
    }

    private fun renderBucket(
        renderables: MutableList<Renderable>, sortFactor: Double, camera: Camera, uniformDecorator: (UniformSupplier) -> UniformSupplier
    ) {
//        val visibleRenderables = renderables.filter { isVisible(it) }
//            .sortedBy {
//                val worldBB = it.mesh.modelBoundingBox!!.transform(it.transform)
//                (camera.mat4() * worldBB.center()).z * sortFactor
//            }
//        visibleRenderables.forEach { it.render(contextUniforms) }
        // TODO
        renderables.forEach { it.render(uniformDecorator) }
    }

//    private fun isVisible(renderable: Renderable): Boolean {
//        return if (renderable.worldBoundingBox == null) {
//            true
//        } else {
//            renderable.worldBoundingBox!!.isIn(projection.mat4() * camera.mat4())
//        }
//    }
//

    private fun renderTo(fb: GpuFrameBuffer?, block: () -> Unit) {
        if (fb == null) block() else fb.exec { block() }
    }

    private class TouchBox(private val x: Int, private val y: Int, private val w: Int, private val h: Int, private val handler: TouchHandler) {
        fun touch(touchEvent: TouchEvent) {
            // TODO: process drag-out as UP
            if (touchEvent.x > x && touchEvent.x < x + w && touchEvent.y > y && touchEvent.y < y + h) {
                handler(touchEvent)
            }
        }
    }
}

