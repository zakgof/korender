package com.zakgof.korender.declaration

import com.zakgof.korender.impl.engine.Bucket
import com.zakgof.korender.input.TouchEvent
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

class InstancedBillboardsContext {

    val instances = mutableListOf<BillboardInstance>()

    fun Instance(pos: Vec3, scale: Vec2 = Vec2.ZERO, phi: Float = 0f) =
        instances.add(BillboardInstance(pos, scale, phi))
}

class BillboardInstance(val pos: Vec3, val scale: Vec2 = Vec2.ZERO, val phi: Float = 0f)

class RenderableInstance(val transform: Transform)


class InstancedRenderablesContext {

    val instances = mutableListOf<RenderableInstance>()

    fun Instance(transform: Transform) =
        instances.add(RenderableInstance(transform))
}

data class ShaderDeclaration(
    val vertFile: String,
    val fragFile: String,
    val defs: Set<String>
)

data class FilterDeclaration(val fragment: String, val uniforms: UniformSupplier)

class RenderableDeclaration(
    val mesh: MeshDeclaration,
    val shader: ShaderDeclaration,
    val uniforms: UniformSupplier,
    val transform: Transform = Transform(),
    val bucket: Bucket = Bucket.OPAQUE
)

class ContainerContext(private val declaration: ElementDeclaration.ContainerDeclaration) {
    fun Row(block: ContainerContext.() -> Unit) {
        val row = ElementDeclaration.ContainerDeclaration(Direction.Horizontal)
        ContainerContext(row).apply(block)
        declaration.add(row)
    }

    fun Column(block: ContainerContext.() -> Unit) {
        val column = ElementDeclaration.ContainerDeclaration(Direction.Vertical)
        ContainerContext(column).apply(block)
        declaration.add(column)
    }

    fun Text(
        id: Any,
        fontResource: String,
        height: Int,
        text: String,
        color: Color,
        onTouch: TouchHandler = {}
    ) {
        declaration.add(ElementDeclaration.TextDeclaration(id, fontResource, height, text, color, onTouch))
    }

    fun Filler() {
        declaration.add(ElementDeclaration.FillerDeclaration())
    }

    fun Image(imageResource: String, width: Int, height: Int, onTouch: TouchHandler = {}) {
        declaration.add(ElementDeclaration.ImageDeclaration(imageResource, width, height, onTouch))
    }
}

sealed class ElementDeclaration {

    class FillerDeclaration : ElementDeclaration()
    class TextDeclaration(val id: Any, val fontResource: String, val height: Int, val text: String, val color: Color, val onTouch: TouchHandler) : ElementDeclaration()
    class ImageDeclaration(val imageResource: String, val width: Int, val height: Int, val onTouch: TouchHandler) : ElementDeclaration()
    class ContainerDeclaration(val direction: Direction) : ElementDeclaration() {

        val elements = mutableListOf<ElementDeclaration>()
        fun add(element: ElementDeclaration) = elements.add(element)
    }
}

enum class Direction {
    Vertical,
    Horizontal
}

fun onClick(touchEvent: TouchEvent, clickHandler: () -> Unit) {
    if (touchEvent.type == TouchEvent.Type.DOWN) {
        clickHandler()
    }
}

data class TextureDeclaration(val textureResource: String) // TODO filter and wrap

class ShadowDeclaration(val mapSize: Int) {

    val renderables = mutableListOf<RenderableDeclaration>()
    fun addRenderable(renderableDeclaration: RenderableDeclaration) =
        renderables.add(renderableDeclaration)
}

data class FrameBufferDeclaration(val id: String, val width: Int, val height: Int, val withDepth: Boolean)