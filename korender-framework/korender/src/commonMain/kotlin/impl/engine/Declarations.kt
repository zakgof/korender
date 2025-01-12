package com.zakgof.korender.impl.engine

import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.RenderingOption
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.impl.context.Direction
import com.zakgof.korender.impl.material.DynamicUniforms
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

internal class SceneDeclaration {
    val pointLights = mutableListOf<PointLightDeclaration>()
    val directionalLights = mutableListOf<DirectionalLightDeclaration>()
    var ambientLightColor = Color(1.0f, 0.15f, 0.15f, 0.15f)
    val renderables = mutableListOf<RenderableDeclaration>()
    val guis = mutableListOf<ElementDeclaration.Container>()
    val gltfs = mutableListOf<GltfDeclaration>()
    var filters = mutableListOf<MaterialDeclaration>()
}

internal class BillboardInstance(val pos: Vec3, val scale: Vec2 = Vec2.ZERO, val phi: Float = 0f)

internal class MeshInstance(val transform: Transform)

internal data class ShaderDeclaration(
    val vertFile: String,
    val fragFile: String,
    val defs: Set<String> = setOf(),
    val options: Set<RenderingOption> = setOf(),
    val plugins: Map<String, String> = mapOf()
)

internal class RenderableDeclaration(
    val mesh: MeshDeclaration,
    val shader: ShaderDeclaration,
    val uniforms: DynamicUniforms,
    val transform: Transform = Transform(),
    val bucket: Bucket = Bucket.OPAQUE
)

internal class MaterialDeclaration(
    val shader: ShaderDeclaration,
    val uniforms: DynamicUniforms
)

internal sealed class ElementDeclaration {

    class Filler : ElementDeclaration()
    class Text(
        val id: Any,
        val fontResource: String,
        val height: Int,
        val text: String,
        val color: Color,
        val onTouch: TouchHandler
    ) : ElementDeclaration()

    class Image(
        val imageResource: String,
        val width: Int,
        val height: Int,
        val marginTop: Int,
        val marginBottom: Int,
        val marginLeft: Int,
        val marginRight: Int,
        val onTouch: TouchHandler
    ) : ElementDeclaration() {
        val fullWidth = width + marginLeft + marginRight
        val fullHeight = height + marginTop + marginBottom
    }

    class Container(val direction: Direction) : ElementDeclaration() {

        val elements = mutableListOf<ElementDeclaration>()
        fun add(element: ElementDeclaration) = elements.add(element)
    }
}

internal data class FrameBufferDeclaration(
    val id: String,
    val width: Int,
    val height: Int,
    val colorTextures: Int,
    val withDepth: Boolean
)

internal class ShadowDeclaration {
    val cascades = mutableListOf<CascadeDeclaration>()
}

internal data class CascadeDeclaration(val mapSize: Int, val near: Float, var far: Float)

internal class GltfDeclaration(val gltfResource: String, val transform: Transform = Transform()) {
    override fun equals(other: Any?): Boolean = (other is GltfDeclaration && other.gltfResource == gltfResource)
    override fun hashCode(): Int = gltfResource.hashCode()
}

internal class PointLightDeclaration(val position: Vec3, val color: Color)

internal class DirectionalLightDeclaration(val direction: Vec3, val color: Color, val shadowDeclaration: ShadowDeclaration)