package com.zakgof.korender.impl.engine

import com.zakgof.korender.KorenderException
import com.zakgof.korender.SceneDeclaration
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.camera.Camera
import com.zakgof.korender.declaration.Direction
import com.zakgof.korender.declaration.InstancedBillboardsContext
import com.zakgof.korender.declaration.InstancedRenderablesContext
import com.zakgof.korender.declaration.MeshDeclaration
import com.zakgof.korender.declaration.StandardMaterialOption
import com.zakgof.korender.declaration.TextureDeclaration
import com.zakgof.korender.declaration.UniformSupplier
import com.zakgof.korender.impl.font.Fonts
import com.zakgof.korender.impl.geometry.Geometry
import com.zakgof.korender.impl.gl.VGL11
import com.zakgof.korender.impl.gpu.GpuFrameBuffer
import com.zakgof.korender.impl.material.MapUniformSupplier
import com.zakgof.korender.impl.material.Shaders
import com.zakgof.korender.input.TouchEvent
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.projection.Projection
import java.util.EnumSet
import java.util.function.Predicate
import kotlin.math.max

internal class Scene(sceneDeclaration: SceneDeclaration, private val inventory: Inventory, private val camera: Camera, private val width: Int, private val height: Int) {

    private val shadower: Shadower?
    private val filters: List<Filter>
    private val filterFrameBuffers: List<GpuFrameBuffer>

    private val opaques = mutableListOf<Renderable>()
    private val transparents = mutableListOf<Renderable>()
    private val skies = mutableListOf<Renderable>()
    private val screens = mutableListOf<Renderable>()
    private val shadowCasters = mutableListOf<Renderable>()

    private val touchBoxes = mutableListOf<TouchBox>();
    val touchBoxesHandler: Predicate<TouchEvent> = Predicate { evt ->
        touchBoxes.any { it.touch(evt) }
    }

    init {
        shadower = sceneDeclaration.shadow?.let { createShadower(inventory, it) }
        val shadowCascades = shadower?.cascadeNumber ?: 0
        sceneDeclaration.renderables.forEach {
            val renderable = createRenderable(it, shadowCascades)
            when (it.bucket) {
                Bucket.OPAQUE -> opaques.add(renderable)
                Bucket.SKY -> skies.add(renderable)
                Bucket.TRANSPARENT -> transparents.add(renderable)
                Bucket.SCREEN -> screens.add(renderable)
            }
            if (isShadowCaster(it)) {
                shadowCasters.add(renderable)
            }
        }
        filters = sceneDeclaration.filters.map { createFilter(it) }
        filterFrameBuffers = filterFrameBuffers()
        sceneDeclaration.gui?.let { layoutGui(width, height, it) }
    }

    private fun isShadowCaster(renderableDeclaration: RenderableDeclaration): Boolean =
        (renderableDeclaration.shader is StandardShaderDeclaration && !renderableDeclaration.shader.options.contains(StandardMaterialOption.NoShadowCast))

    private fun createShadower(inventory: Inventory, shadowDecl: ShadowDeclaration): Shadower =
        CascadeShadower(inventory, shadowDecl.cascades)

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
                    else -> throw KorenderException("")
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
                    is ElementDeclaration.Filler -> {}
                    else -> throw KorenderException("")
                }
                currX += childWidth
            }
        }
    }

    private fun createImage(declaration: ElementDeclaration.Image, x: Int, y: Int) {
        screens.add(
            Renderable(
                mesh = inventory.mesh(MeshDeclaration.ImageQuad), shader = inventory.shader(Shaders.imageQuadDeclaration), uniforms = MapUniformSupplier(
                    Pair("pos", Vec2((x.toFloat() + declaration.marginLeft.toFloat()) / width, 1.0f - (y.toFloat() + declaration.marginTop.toFloat() + declaration.height.toFloat()) / height)),
                    Pair("size", Vec2(declaration.width.toFloat() / width, declaration.height.toFloat() / height)),
                    Pair("imageTexture", inventory.texture(TextureDeclaration(declaration.imageResource)))
                )
            )
        )
        touchBoxes.add(TouchBox(x + declaration.marginLeft, y + declaration.marginTop, declaration.width, declaration.height, declaration.onTouch))
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
            is ElementDeclaration.Image -> Size(element.fullWidth, element.fullHeight)
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

            else -> throw KorenderException("")
        }
        sizes[element] = size
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

    private fun createRenderable(declaration: RenderableDeclaration, shadowCascades: Int): Renderable {

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

        val shader = inventory.shader(toCustomShader(declaration.shader, shadowCascades))
        val uniforms = declaration.uniforms
        val transform = declaration.transform
        return Renderable(mesh, shader, uniforms, transform)
    }

    private fun toCustomShader(shader: ShaderDeclaration, shadowCascades: Int): CustomShaderDeclaration {
        return when (shader) {
            is CustomShaderDeclaration -> shader
            is StandardShaderDeclaration -> CustomShaderDeclaration("standard.vert", "standard.frag", convertShaderOptions(shader.options, shadowCascades))
            else -> throw KorenderException("")
        }
    }

    // TODO move this to standard shader classes or something
    private fun convertShaderOptions(options: EnumSet<StandardMaterialOption>, shadowCascades: Int): Set<String> {
        val set = mutableSetOf<String>()
        if (!options.contains(StandardMaterialOption.NoShadowReceive)) {
            for (s in 0..<shadowCascades) {
                set.add("SHADOW_RECEIVER$s")
            }
        }
        options.forEach {
            when (it) {
                StandardMaterialOption.Color -> set.add("COLOR")
                StandardMaterialOption.Triplanar -> set.add("TRIPLANAR")
                StandardMaterialOption.Aperiodic -> set.add("APERIODIC")
                StandardMaterialOption.NormalMap -> set.add("NORMAL_MAP")
                StandardMaterialOption.Detail -> set.add("DETAIL")
                StandardMaterialOption.NoLight -> set.add("NO_LIGHT")
                StandardMaterialOption.Pcss -> set.add("PCSS")
                else -> {}
            }
        }
        return set
    }

    private fun createFilter(declaration: FilterDeclaration): Filter {
        val shader = inventory.shader(CustomShaderDeclaration("screen.vert", declaration.fragment, setOf()))
        return Filter(shader, declaration.uniforms)
    }

    fun render(context: Map<String, Any?>, projection: Projection, camera: Camera, light: Vec3) {
        val shadowUniforms: UniformSupplier = shadower?.render(projection, camera, light, shadowCasters) ?: UniformSupplier {}
        val uniformDecorator: (UniformSupplier) -> UniformSupplier = {
            UniformSupplier { key ->
                var value = it[key] ?: context[key] ?: shadowUniforms[key]
                if (value is TextureDeclaration) {
                    value = inventory.texture(value)
                }
                value
            }
        }
        val prevFrameContext = mutableMapOf<String, Any?>()
        for (p in 0..filters.size) {
            val frameBuffer = if (p == filters.size) null else filterFrameBuffers[p % 2]
            renderTo(frameBuffer) {
                if (p == 0) {
                    VGL11.glClear(VGL11.GL_COLOR_BUFFER_BIT or VGL11.GL_DEPTH_BUFFER_BIT)
                    renderStage(camera, uniformDecorator)
                } else {
                    val filter = filters[p - 1]
                    VGL11.glClear(VGL11.GL_COLOR_BUFFER_BIT or VGL11.GL_DEPTH_BUFFER_BIT)
                    filter.gpuShader.render(
                        filter.uniforms + context + prevFrameContext,
                        inventory.mesh(MeshDeclaration.ScreenQuad).gpuMesh
                    )
                }
                if (frameBuffer == null) {
                    renderScreens(uniformDecorator)
                }
            }
            prevFrameContext["filterColorTexture"] = frameBuffer?.colorTexture
            prevFrameContext["filterDepthTexture"] = frameBuffer?.depthTexture
        }
    }

    private fun renderScreens(uniformDecorator: (UniformSupplier) -> UniformSupplier) {
        screens.forEach { it.render(uniformDecorator) }
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

    private fun renderStage(camera: Camera, uniformDecorator: (UniformSupplier) -> UniformSupplier) {

        VGL11.glViewport(0, 0, width, height)
        VGL11.glEnable(VGL11.GL_CULL_FACE)
        VGL11.glCullFace(VGL11.GL_BACK)
        VGL11.glClear(VGL11.GL_COLOR_BUFFER_BIT or VGL11.GL_DEPTH_BUFFER_BIT)
        renderBucket(opaques, -1.0, camera, uniformDecorator)
        skies.forEach { it.render(uniformDecorator) }
        VGL11.glDepthMask(false)
        renderBucket(transparents, 1.0, camera, uniformDecorator)
        VGL11.glDepthMask(true)
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
        fun touch(touchEvent: TouchEvent): Boolean {
            // TODO: process drag-out as UP
            if (touchEvent.x > x && touchEvent.x < x + w && touchEvent.y > y && touchEvent.y < y + h) {
                handler(touchEvent)
                return true
            }
            return false
        }
    }
}

