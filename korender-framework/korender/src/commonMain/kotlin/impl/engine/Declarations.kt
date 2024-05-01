package com.zakgof.korender.impl.engine

import com.zakgof.korender.TouchHandler
import com.zakgof.korender.declaration.Direction
import com.zakgof.korender.declaration.MeshDeclaration
import com.zakgof.korender.declaration.UniformSupplier
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3


internal class BillboardInstance(val pos: Vec3, val scale: Vec2 = Vec2.ZERO, val phi: Float = 0f)

internal class MeshInstance(val transform: Transform)

internal data class ShaderDeclaration(val vertFile: String, val fragFile: String, val defs: Set<String>)

internal data class FilterDeclaration(val fragment: String, val uniforms: UniformSupplier)

internal class RenderableDeclaration(
    val mesh: MeshDeclaration,
    val shader: ShaderDeclaration,
    val uniforms: UniformSupplier,
    val transform: Transform = Transform(),
    val bucket: Bucket = Bucket.OPAQUE
)

internal sealed class ElementDeclaration {

    class Filler : ElementDeclaration()
    class Text(val id: Any, val fontResource: String, val height: Int, val text: String, val color: Color, val onTouch: TouchHandler) : ElementDeclaration()
    class Image(val imageResource: String, val width: Int, val height: Int, val onTouch: TouchHandler) : ElementDeclaration()
    class Container(val direction: Direction) : ElementDeclaration() {

        val elements = mutableListOf<ElementDeclaration>()
        fun add(element: ElementDeclaration) = elements.add(element)
    }
}

internal data class FrameBufferDeclaration(val id: String, val width: Int, val height: Int, val withDepth: Boolean)

internal class ShadowDeclaration(val mapSize: Int, val cascades: List<Float>) {

    internal val renderables = mutableListOf<RenderableDeclaration>()
    fun addRenderable(renderableDeclaration: RenderableDeclaration) =
        renderables.add(renderableDeclaration)
}