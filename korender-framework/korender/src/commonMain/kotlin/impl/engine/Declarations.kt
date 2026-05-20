package com.zakgof.korender.impl.engine

import com.zakgof.korender.BaseMaterialScope
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.ModelInfo
import com.zakgof.korender.PostProcessingEffect
import com.zakgof.korender.ShadowAlgorithmDeclaration
import com.zakgof.korender.TerrainMaterialScope
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.impl.context.DefaultFrameScope
import com.zakgof.korender.impl.context.Direction
import com.zakgof.korender.impl.context.NodeContext
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.glgpu.UniformPack
import com.zakgof.korender.impl.material.InternalDecalMaterial
import com.zakgof.korender.impl.material.InternalMaterial
import com.zakgof.korender.impl.material.InternalMultiPassEffect
import com.zakgof.korender.impl.material.InternalPostProcessingMaterial
import com.zakgof.korender.impl.material.InternalShadingMaterial
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.scope.BillboardInstancingDeclaration
import com.zakgof.korender.scope.BillboardInstancingParameter
import com.zakgof.korender.scope.InstancingDeclaration
import com.zakgof.korender.scope.InstancingParameter
import com.zakgof.korender.scope.ModelInstancingDeclaration

internal class SceneDeclaration {
    val pointLights = mutableListOf<PointLightDeclaration>()
    val directionalLights = mutableListOf<DirectionalLightDeclaration>()
    var ambientLightColor = white(0.3f)

    val opaques = mutableListOf<RenderableDeclaration>()
    val transparents = mutableListOf<RenderableDeclaration>()
    val skies = mutableListOf<RenderableDeclaration>()

    val guis = mutableListOf<ElementDeclaration.Container>()
    val models = mutableListOf<ModelDeclaration>()
    val heightFields = mutableListOf<HeightFieldDeclaration>()
    var filters = mutableListOf<InternalFilterDeclaration>()
    var deferredShadingDeclaration: DeferredShadingDeclaration? = null
    val envCaptures = mutableMapOf<String, CaptureContext>()
    val frameCaptures = mutableMapOf<String, CaptureContext>()
    var loaderSceneDeclaration: SceneDeclaration? = null
    var loaderForced = false

    fun append(renderableDeclaration: RenderableDeclaration) =
        if (renderableDeclaration.transparent)
            transparents += renderableDeclaration
        else
            opaques += renderableDeclaration
}

internal class DeferredShadingDeclaration(val nodeContext: NodeContext) {
    var shadingMaterial = InternalShadingMaterial()
    var shadingEffects =  mutableListOf<InternalMultiPassEffect>()
    val postShadingEffects = mutableListOf<InternalMultiPassEffect>()
    val decals = mutableListOf<InternalDecalDeclaration>()
}

internal data class SsaoDeclaration(
    val downsample: Int = 2,
    val sampleCount: Int = 16,
    val radius: Float = 0.75f,
    val bias: Float = 0.03f,
    val intensity: Float = 1.0f,
    val blurRadius: Float = 5f,
)

internal data class HbaoDeclaration(
    val downsample: Int = 2,
    val sampleCount: Int = 16,
    val radius: Float = 0.75f,
    val bias: Float = 0.02f,
    val intensity: Float = 1.0f,
    val blurRadius: Float = 5f,
)

internal class InternalDecalDeclaration(
    val position: Vec3,
    val look: Vec3,
    val up: Vec3,
    val size: Float,
    val material: InternalDecalMaterial,
)

internal class BillboardInstance(
    val pos: Vec3?,
    val scale: Vec2?,
    val rotation: Float?,
    val color: ColorRGBA?,
    val colorTextureIndex: Int?
)

internal class MeshInstance(
    val transform: Transform?,
    val jointMatrices: List<Mat4>?,
    val color: ColorRGBA?,
    val metallic: Float?,
    val roughness: Float?,
    val colorTextureIndex: Int?
)

internal data class ShaderDeclaration(
    val vertFile: String,
    val fragFile: String,
    val defs: Long,
    val plugins1: Long,
    val plugins2: Long,
    val uniformPack: UniformPack,
    override val nodeContext: NodeContext,
) : NodeKeeper {
    override fun equals(other: Any?): Boolean =
        other is ShaderDeclaration &&
                vertFile == other.vertFile &&
                fragFile == other.fragFile &&
                defs == other.defs &&
                plugins1 == other.plugins1 &&
                plugins2 == other.plugins2

    override fun hashCode(): Int = listOf(vertFile, fragFile, defs, plugins1, plugins2).hashCode()  // TODO
}

internal class RenderableDeclaration(
    val material: InternalMaterial,
    val mesh: MeshDeclaration,
    val transform: Transform = Transform.IDENTITY,
    val transparent: Boolean,
    override val nodeContext: NodeContext,
) : NodeKeeper

internal sealed interface ElementDeclaration {

    class Filler : ElementDeclaration

    class Text(
        val id: String,
        val fontResource: String,
        val height: Float,
        val text: String,
        val color: ColorRGBA,
        val static: Boolean,
        val onTouch: TouchHandler,
        override val nodeContext: NodeContext,
    ) : ElementDeclaration, NodeKeeper

    class Image(
        val id: String,
        val imageResource: String,
        val width: Float,
        val height: Float,
        val marginTop: Float,
        val marginBottom: Float,
        val marginLeft: Float,
        val marginRight: Float,
        val onTouch: TouchHandler,
        override val nodeContext: NodeContext,
    ) : ElementDeclaration, NodeKeeper {
        val fullWidth = width + marginLeft + marginRight
        val fullHeight = height + marginTop + marginBottom
    }

    class Container(val direction: Direction) : ElementDeclaration {
        val elements = mutableListOf<ElementDeclaration>()
        fun add(element: ElementDeclaration) = elements.add(element)
    }
}

internal data class FrameBufferDeclaration(
    val id: String,
    val width: Int,
    val height: Int,
    val colorTexturePresets: List<GlGpuTexture.Preset>,
    val withDepth: Boolean,
    override val nodeContext: NodeContext,
) : NodeKeeper

internal data class CubeFrameBufferDeclaration(
    val id: String,
    val width: Int,
    val height: Int,
    val withDepth: Boolean,
    override val nodeContext: NodeContext,
) : NodeKeeper

internal class ShadowDeclaration {
    val cascades = mutableListOf<CascadeDeclaration>()
}

internal data class CascadeDeclaration(val mapSize: Int, val near: Float, val far: Float, val fixedYRange: Pair<Float, Float>?, val algorithm: ShadowAlgorithmDeclaration)

internal class ModelDeclaration(
    val resource: String,
    val transform: Transform,
    val instancingDeclaration: InternalModelInstancingDeclaration?,
    val time: Float,
    val animation: Int,
    val onUpdate: (ModelInfo) -> Unit,
    val materialModifier: BaseMaterialScope.() -> Unit,
    override val nodeContext: NodeContext,
) : NodeKeeper {
    override fun equals(other: Any?): Boolean = (other is ModelDeclaration && other.resource == resource)
    override fun hashCode(): Int = resource.hashCode()
}

internal class HeightFieldDeclaration(
    val id: String,
    val cellSize: Float,
    val hg: Int,
    val rings: Int,
    val block: TerrainMaterialScope.() -> Unit,
    val frameScope: DefaultFrameScope
) : NodeKeeper {
    override val nodeContext = frameScope.nodeContext
    override fun equals(other: Any?): Boolean = (other is HeightFieldDeclaration && other.id == id)
    override fun hashCode(): Int = id.hashCode()
}

internal class ModelInstance(val transform: Transform, val time: Float?, val animation: Int?)

internal class PointLightDeclaration(val position: Vec3, val color: ColorRGB, val attenuation: Vec3)

internal class DirectionalLightDeclaration(val direction: Vec3, val color: ColorRGB, val shadowDeclaration: ShadowDeclaration)

internal class InternalInstancingDeclaration(val id: String, val count: Int, val dynamic: Boolean, val parameters: List<InstancingParameter>, val instancer: () -> List<MeshInstance>) : InstancingDeclaration

internal class InternalModelInstancingDeclaration(val id: String, val count: Int, val dynamic: Boolean, val instancer: () -> List<ModelInstance>) : ModelInstancingDeclaration

internal class InternalBillboardInstancingDeclaration(val id: String, val count: Int, val dynamic: Boolean, val parameters: List<BillboardInstancingParameter>, val instancer: () -> List<BillboardInstance>) : BillboardInstancingDeclaration

internal class InternalFilterDeclaration(val passes: List<InternalPassDeclaration>) : PostProcessingEffect

internal class InternalPassDeclaration(val mapping: Map<String, String>, val material: InternalPostProcessingMaterial, val sceneDeclaration: SceneDeclaration?, val target: FrameTarget, override val nodeContext: NodeContext) : NodeKeeper

internal data class FrameTarget(val colorOutput: String, val depthOutput: String, val downSample: Int = 1) {
    companion object {
        val default = FrameTarget("colorTexture", "depthTexture")
    }
}
