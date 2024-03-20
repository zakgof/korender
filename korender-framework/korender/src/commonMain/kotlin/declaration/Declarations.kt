package com.zakgof.korender.declaration

import com.zakgof.korender.Bucket
import com.zakgof.korender.material.UniformSupplier
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

object MeshDeclarations {

    fun cube(halfSide: Float = 0.5f) = MeshDeclaration.CubeDeclaration(halfSide)
    fun sphere(radius: Float = 1.0f) = MeshDeclaration.SphereDeclaration(radius)
    fun obj(objFile: String) = MeshDeclaration.ObjDeclaration(objFile)
}

sealed interface MeshDeclaration {

    data class CubeDeclaration(val halfSide: Float) : MeshDeclaration

    data class SphereDeclaration(val radius: Float) : MeshDeclaration

    data class ObjDeclaration(val objFile: String) : MeshDeclaration

    data object BillboardDeclaration : MeshDeclaration // TODO position scale and shit
    data object ImageQuadDeclaration : MeshDeclaration

    data object SkyDeclaration : MeshDeclaration

    data class InstancedRenderableDeclaration(
        val id: Any,
        val count: Int,
        val mesh: MeshDeclaration,
        val material: MaterialDeclaration,
        val static: Boolean,
        val block: InstancedRenderablesContext.() -> Unit,
    ) : MeshDeclaration {
        override fun equals(other: Any?): Boolean =
            other is InstancedRenderableDeclaration && other.id == id

        override fun hashCode(): Int = id.hashCode()
    }


    data class InstancedBillboardDeclaration(
        val id: Any,
        val count: Int,
        val zSort: Boolean,
        val block: InstancedBillboardsContext.() -> Unit
    ) : MeshDeclaration {
        override fun equals(other: Any?): Boolean =
            other is InstancedBillboardDeclaration && other.id == id

        override fun hashCode(): Int = id.hashCode()
    }
}

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

class MaterialDeclaration(val shader: ShaderDeclaration, val uniforms: UniformSupplier)

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

    fun Text(id: Any, fontResource: String, height: Int, text: String, color: Color) {
        declaration.add(ElementDeclaration.TextDeclaration(id, fontResource, height, text, color))
    }

    fun Filler() {
        declaration.add(ElementDeclaration.FillerDeclaration())
    }

    fun Image(imageResource: String, width: Int, height: Int) {
        declaration.add(ElementDeclaration.ImageDeclaration(imageResource, width, height))
    }
}

sealed class ElementDeclaration {

    class FillerDeclaration: ElementDeclaration()
    class TextDeclaration(val id: Any, val fontResource: String, val height: Int, val text: String, val color: Color) : ElementDeclaration()
    class ImageDeclaration(val imageResource: String, val width: Int, val height: Int) : ElementDeclaration()
    class ContainerDeclaration(val direction: Direction) : ElementDeclaration() {

        val elements = mutableListOf<ElementDeclaration>()
        fun add(element: ElementDeclaration) = elements.add(element)
    }
}

enum class Direction {
    Vertical,
    Horizontal
}