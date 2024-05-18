package com.zakgof.korender.impl.engine

import com.zakgof.korender.KorenderException
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.declaration.MeshDeclaration
import com.zakgof.korender.declaration.StandardMaterialOption
import com.zakgof.korender.declaration.UniformSupplier
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import java.util.EnumSet

internal class SceneDeclaration {

    var gui: ElementDeclaration.Container? = null
    var shadow: ShadowDeclaration? = null
    val renderables = mutableListOf<RenderableDeclaration>()
    val filters = mutableListOf<FilterDeclaration>()
    fun add(renderable: RenderableDeclaration) = renderables.add(renderable)
    fun add(filter: FilterDeclaration) = filters.add(filter)
    fun addShadow(shadow: ShadowDeclaration) {
        if (this.shadow != null) {
            throw KorenderException("Only one Shadow declaration is allowed")
        }
        this.shadow = shadow
    }
}

internal class BillboardInstance(val pos: Vec3, val scale: Vec2 = Vec2.ZERO, val phi: Float = 0f)

internal class MeshInstance(val transform: Transform)

internal sealed interface ShaderDeclaration

internal data class CustomShaderDeclaration(val vertFile: String, val fragFile: String, val defs: Set<String> = setOf()) : ShaderDeclaration

internal data class StandardShaderDeclaration(val options: EnumSet<StandardMaterialOption>) : ShaderDeclaration

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
    class Image(val imageResource:String, val width: Int, val height: Int, val marginTop: Int, val marginBottom: Int, val marginLeft: Int, val marginRight: Int,val onTouch: TouchHandler) : ElementDeclaration() {
        val fullWidth = width + marginLeft + marginRight
        val fullHeight = height + marginTop + marginBottom
    }
    class Container(val direction: Direction) : ElementDeclaration() {

        val elements = mutableListOf<ElementDeclaration>()
        fun add(element: ElementDeclaration) = elements.add(element)
    }
}

internal data class FrameBufferDeclaration(val id: String, val width: Int, val height: Int, val withDepth: Boolean)

internal class ShadowDeclaration() {

    internal val cascades = mutableListOf<CascadeDeclaration>()
    fun addCascade(cascadeDeclaration: CascadeDeclaration) =
        cascades.add(cascadeDeclaration)
}

internal data class CascadeDeclaration(val mapSize: Int, val near: Float, var far: Float)