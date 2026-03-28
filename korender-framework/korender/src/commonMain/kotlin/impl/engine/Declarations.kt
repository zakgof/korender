package com.zakgof.korender.impl.engine

import com.zakgof.korender.BaseMaterialContext
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.PostProcessingEffect
import com.zakgof.korender.PostShadingEffect
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.ShadowAlgorithmDeclaration
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.context.BillboardInstancingDeclaration
import com.zakgof.korender.context.GltfInstancingDeclaration
import com.zakgof.korender.context.InstancingDeclaration
import com.zakgof.korender.gltf.GltfUpdate
import com.zakgof.korender.impl.context.Direction
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.glgpu.UniformSupplier
import com.zakgof.korender.impl.material.InternalDecalMaterial
import com.zakgof.korender.impl.material.InternalMaterial
import com.zakgof.korender.impl.material.InternalMaterialModifier
import com.zakgof.korender.impl.material.InternalPostProcessingMaterial
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

internal class SceneDeclaration {
    val pointLights = mutableListOf<PointLightDeclaration>()
    val directionalLights = mutableListOf<DirectionalLightDeclaration>()
    var ambientLightColor = white(0.3f)

    val opaques = mutableListOf<RenderableDeclaration>()
    val transparents = mutableListOf<RenderableDeclaration>()
    val skies = mutableListOf<RenderableDeclaration>()

    val guis = mutableListOf<ElementDeclaration.Container>()
    val gltfs = mutableListOf<GltfDeclaration>()
    var filters = mutableListOf<InternalFilterDeclaration>()
    var deferredShadingDeclaration: DeferredShadingDeclaration? = null
    val envCaptures = mutableMapOf<String, EnvCaptureContext>()
    val frameCaptures = mutableMapOf<String, FrameCaptureContext>()
    var loaderSceneDeclaration: SceneDeclaration? = null

    fun append(renderableDeclaration: RenderableDeclaration) =
        if (renderableDeclaration.transparent)
            transparents += renderableDeclaration
        else
            opaques += renderableDeclaration
}

internal class DeferredShadingDeclaration() {
    var postShadingEffects = mutableListOf<PostShadingEffect>()
    var decals = mutableListOf<InternalDecalDeclaration>()
}

internal class InternalDecalDeclaration(
    val position: Vec3,
    val look: Vec3,
    val up: Vec3,
    val size: Float,
    val material: InternalDecalMaterial
)

internal class BillboardInstance(val pos: Vec3, val scale: Vec2 = Vec2.ZERO, val phi: Float = 0f)

internal class MeshInstance(
    val transform: Transform,
    val jointMatrices: List<Mat4>?
)

internal data class ShaderDeclaration(
    val vertFile: String,
    val fragFile: String,
    val defs: Set<String> = setOf(),
    val plugins: Map<String, String> = mapOf(),
    val uniformSuppliers: List<UniformSupplier>,
    override val retentionPolicy: RetentionPolicy
) : Retentionable
{
    override fun equals(other: Any?): Boolean =
        other is ShaderDeclaration &&
            vertFile == other.vertFile &&
            fragFile == other.fragFile &&
            defs == other.defs &&
            plugins == other.plugins

    override fun hashCode(): Int = listOf(vertFile, fragFile, defs, plugins).hashCode()
}

internal class RenderableDeclaration(
    val material: InternalMaterial,
    val modifiers: List<InternalMaterialModifier> = listOf(), // TODO poor design
    val mesh: MeshDeclaration,
    val transform: Transform = Transform.IDENTITY,
    val transparent: Boolean,
    override val retentionPolicy: RetentionPolicy
) : Retentionable

internal sealed interface ElementDeclaration {

    class Filler : ElementDeclaration

    class Text(
        val id: String,
        val fontResource: String,
        val height: Int,
        val text: String,
        val color: ColorRGBA,
        val static: Boolean,
        val onTouch: TouchHandler,
        override val retentionPolicy: RetentionPolicy
    ) : ElementDeclaration, Retentionable

    class Image(
        val id: String,
        val imageResource: String,
        val width: Int,
        val height: Int,
        val marginTop: Int,
        val marginBottom: Int,
        val marginLeft: Int,
        val marginRight: Int,
        val onTouch: TouchHandler,
        override val retentionPolicy: RetentionPolicy
    ) : ElementDeclaration, Retentionable {
        val fullWidth = width + marginLeft + marginRight
        val fullHeight = height + marginTop + marginBottom
    }

    class Container(val direction: Direction) : ElementDeclaration {
        val elements = mutableListOf<ElementDeclaration>()
        fun add(element: ElementDeclaration) = elements.add(element)
    }
}

internal class TransientProperty<T>(val property: T) {
    override fun equals(other: Any?) = true
    override fun hashCode() = 0
}

internal data class FrameBufferDeclaration(
    val id: String,
    val width: Int,
    val height: Int,
    val colorTexturePresets: List<GlGpuTexture.Preset>,
    val withDepth: Boolean,
    val retentionPolicyHolder: TransientProperty<RetentionPolicy>
) : Retentionable {
    override val retentionPolicy = retentionPolicyHolder.property
}

internal data class CubeFrameBufferDeclaration(
    val id: String,
    val width: Int,
    val height: Int,
    val withDepth: Boolean,
    val retentionPolicyHolder: TransientProperty<RetentionPolicy>
) : Retentionable {
    override val retentionPolicy = retentionPolicyHolder.property
}

internal class ShadowDeclaration {
    val cascades = mutableListOf<CascadeDeclaration>()
}

internal data class CascadeDeclaration(val mapSize: Int, val near: Float, val far: Float, val fixedYRange: Pair<Float, Float>?, val algorithm: ShadowAlgorithmDeclaration)

internal class GltfDeclaration(
    val resource: String,
    val onUpdate: (GltfUpdate) -> Unit,
    val transform: Transform,
    val time: Float,
    val animation: Int,
    val instancingDeclaration: InternalGltfInstancingDeclaration?,
    val materialModifier: BaseMaterialContext.() -> Unit,
    override val retentionPolicy: RetentionPolicy
) : Retentionable {
    override fun equals(other: Any?): Boolean = (other is GltfDeclaration && other.resource == resource)
    override fun hashCode(): Int = resource.hashCode()
}

internal class GltfInstance(val transform: Transform, val time: Float?, val animation: Int?)

internal class PointLightDeclaration(val position: Vec3, val color: ColorRGB, val attenuation: Vec3)

internal class DirectionalLightDeclaration(val direction: Vec3, val color: ColorRGB, val shadowDeclaration: ShadowDeclaration)

internal class InternalInstancingDeclaration(val id: String, val count: Int, val dynamic: Boolean, val instancer: () -> List<MeshInstance>) : InstancingDeclaration

internal class InternalGltfInstancingDeclaration(val id: String, val count: Int, val dynamic: Boolean, val instancer: () -> List<GltfInstance>) : GltfInstancingDeclaration

internal class InternalBillboardInstancingDeclaration(val id: String, val count: Int, val dynamic: Boolean, val instancer: () -> List<BillboardInstance>) : BillboardInstancingDeclaration

internal class InternalFilterDeclaration(val passes: List<InternalPassDeclaration>) : PostProcessingEffect

internal class InternalPassDeclaration(val mapping: Map<String, String>, val material: InternalPostProcessingMaterial, val sceneDeclaration: SceneDeclaration?, val target: FrameTarget, override val retentionPolicy: RetentionPolicy) : Retentionable

internal data class ReusableFrameBufferDefinition(val pingPong: Int, val width: Int, val height: Int)

internal data class FrameTarget(val width: Int, val height: Int, val colorOutput: String, val depthOutput: String)
