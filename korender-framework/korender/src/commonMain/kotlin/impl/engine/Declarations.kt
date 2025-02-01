package com.zakgof.korender.impl.engine

import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.RenderingOption
import com.zakgof.korender.ShadowAlgorithmDeclaration
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.impl.context.Direction
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.material.DynamicUniforms
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

internal class SceneDeclaration {
    val pointLights = mutableListOf<PointLightDeclaration>()
    val directionalLights = mutableListOf<DirectionalLightDeclaration>()
    var ambientLightColor = white(0.3f)
    val renderables = mutableListOf<RenderableDeclaration>()
    val guis = mutableListOf<ElementDeclaration.Container>()
    val gltfs = mutableListOf<GltfDeclaration>()
    var filters = mutableListOf<List<MaterialModifier>>()
    var deferredShading: Boolean = false
    var compositionModifiers = mutableListOf<MaterialModifier>()
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
    val base: BaseMaterial,
    val materialModifiers: List<MaterialModifier>,
    val mesh: MeshDeclaration,
    val transform: Transform = Transform(),
    val bucket: Bucket = Bucket.OPAQUE
)

internal enum class BaseMaterial {
    Renderable,
    Billboard,
    Screen,
    Sky,
    Composition
}

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
        val color: ColorRGBA,
        val static: Boolean,
        val onTouch: TouchHandler
    ) : ElementDeclaration()

    class Image(
        val id: Any?,
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
    val colorTexturePresets: List<GlGpuTexture.Preset>,
    val withDepth: Boolean
)

internal class ShadowDeclaration {
    val cascades = mutableListOf<CascadeDeclaration>()
}

internal data class CascadeDeclaration(val mapSize: Int, val near: Float, val far: Float, val fixedYRange: Pair<Float, Float>?, val algorithm: ShadowAlgorithmDeclaration)

internal class GltfDeclaration(val gltfResource: String, val animation: Int, val transform: Transform, val time: Float) {
    override fun equals(other: Any?): Boolean = (other is GltfDeclaration && other.gltfResource == gltfResource)
    override fun hashCode(): Int = gltfResource.hashCode()
}

internal class PointLightDeclaration(val position: Vec3, val color: ColorRGB, val attenuation: Vec3)

internal class DirectionalLightDeclaration(val direction: Vec3, val color: ColorRGB, val shadowDeclaration: ShadowDeclaration)