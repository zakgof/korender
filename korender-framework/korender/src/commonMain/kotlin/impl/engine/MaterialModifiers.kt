package com.zakgof.korender.impl.engine

import com.zakgof.korender.ProjectionMode
import com.zakgof.korender.impl.context.NodeContext
import com.zakgof.korender.impl.glgpu.Color3ListGetter
import com.zakgof.korender.impl.glgpu.Color4ListGetter
import com.zakgof.korender.impl.glgpu.ColorRGBGetter
import com.zakgof.korender.impl.glgpu.FloatGetter
import com.zakgof.korender.impl.glgpu.FloatListGetter
import com.zakgof.korender.impl.glgpu.GlGpuShadowTextureList
import com.zakgof.korender.impl.glgpu.GlGpuTextureList
import com.zakgof.korender.impl.glgpu.IntGetter
import com.zakgof.korender.impl.glgpu.IntListGetter
import com.zakgof.korender.impl.glgpu.Mat4Getter
import com.zakgof.korender.impl.glgpu.Mat4ListGetter
import com.zakgof.korender.impl.glgpu.ShadowTextureListGetter
import com.zakgof.korender.impl.glgpu.TextureGetter
import com.zakgof.korender.impl.glgpu.TextureListGetter
import com.zakgof.korender.impl.glgpu.UniformGetter
import com.zakgof.korender.impl.glgpu.Vec3ListGetter
import com.zakgof.korender.impl.material.InternalMaterialModifier
import com.zakgof.korender.impl.material.ResourceTextureDeclaration
import com.zakgof.korender.impl.projection.FrustumProjectionMode
import com.zakgof.korender.impl.projection.LogProjectionMode
import com.zakgof.korender.impl.projection.OrthoProjectionMode
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Mat4

internal class LightMaterialModifier(sc: SceneDeclaration) : InternalMaterialModifier() {

    private val numDirectionalLights = sc.directionalLights.size
    private val directionalLightsDirs = sc.directionalLights.map { it.direction }
    private val directionalLightsColors = sc.directionalLights.map { it.color }
    private val ambientLightColor = sc.ambientLightColor
    private val numPointLights = sc.pointLights.size
    private val pointLightPositions = sc.pointLights.map { it.position }
    private val pointLightColors = sc.pointLights.map { it.color }
    private val pointLightAttenuations = sc.pointLights.map { it.attenuation }
    var dlsti = List(32) { 0 }
    var dlstc = List(32) { 0 }
    var numShadows = 0
    var bsps = listOf<Mat4>()
    var cascades = listOf<ColorRGBA>()
    var yMins = listOf<Float>()
    var yMaxs = listOf<Float>()
    var shadowModes = listOf<Int>()
    var i1 = listOf<Int>()
    var f1 = listOf<Float>()
    var f2 = listOf<Float>()

    override fun uniform(name: String): UniformGetter<*>? =
        when (name) {
            "numDirectionalLights" -> IntGetter<LightMaterialModifier> { it.numDirectionalLights }
            "directionalLightDir[0]" -> Vec3ListGetter<LightMaterialModifier> { it.directionalLightsDirs }
            "directionalLightColor[0]" -> Color3ListGetter<LightMaterialModifier> { it.directionalLightsColors }
            "directionalLightShadowTextureIndex[0]" -> IntListGetter<LightMaterialModifier> { it.dlsti }
            "directionalLightShadowTextureCount[0]" -> IntListGetter<LightMaterialModifier> { it.dlstc }
            "ambientColor" -> ColorRGBGetter<LightMaterialModifier> { it.ambientLightColor }
            "numPointLights" -> IntGetter<LightMaterialModifier> { it.numPointLights }
            "pointLightPos[0]" -> Vec3ListGetter<LightMaterialModifier> { it.pointLightPositions }
            "pointLightColor[0]" -> Color3ListGetter<LightMaterialModifier> { it.pointLightColors }
            "pointLightAttenuation[0]" -> Vec3ListGetter<LightMaterialModifier> { it.pointLightAttenuations }
            "numShadows" -> IntGetter<LightMaterialModifier> { it.numShadows }
            "bsps[0]" -> Mat4ListGetter<LightMaterialModifier> { it.bsps }
            "cascade[0]" -> Color4ListGetter<LightMaterialModifier> { it.cascades }
            "yMin[0]" -> FloatListGetter<LightMaterialModifier> { it.yMins }
            "yMax[0]" -> FloatListGetter<LightMaterialModifier> { it.yMaxs }
            "shadowMode[0]" -> IntListGetter<LightMaterialModifier> { it.shadowModes }
            "i1[0]" -> IntListGetter<LightMaterialModifier> { it.i1 }
            "f1[0]" -> FloatListGetter<LightMaterialModifier> { it.f1 }
            "f2[0]" -> FloatListGetter<LightMaterialModifier> { it.f2 }
            else -> super.uniform(name)
        }
}

internal class ContextMaterialModifier(private val frameContext: FrameContext, rootNodeContext: NodeContext) : InternalMaterialModifier() {

    val noiseTex = ResourceTextureDeclaration("!noise.png", nodeContext = rootNodeContext)
    val fbmTex = ResourceTextureDeclaration("!fbm.png", nodeContext = rootNodeContext)

    var shadowTextures = GlGpuTextureList(List(5) { null }, 5)
    var pcfTextures = GlGpuShadowTextureList(List(5) { null }, 5)

    override fun uniform(name: String): UniformGetter<*>? =
        when (name) {
            "noiseTexture" -> TextureGetter<ContextMaterialModifier> { it.noiseTex }
            "fbmTexture" -> TextureGetter<ContextMaterialModifier> { it.fbmTex }
            "shadowTextures[0]" -> TextureListGetter<ContextMaterialModifier> { it.shadowTextures }
            "pcfTextures[0]" -> ShadowTextureListGetter<ContextMaterialModifier> { it.pcfTextures }
            else -> super.uniform(name)
        }

    override fun collectPlugins(accumulator: MutableMap<String, String>) {
        super.collectPlugins(accumulator)
        accumulator["vprojection"] = frameContext.projection.mode.plugin()
        if (frameContext.projection.mode is LogProjectionMode) {
            accumulator["depth"] = "!shader/plugin/depth.log.frag"
        }
    }

    // TODO: move
    private fun ProjectionMode.plugin() = when (this) {
        is FrustumProjectionMode -> "!shader/plugin/vprojection.frustum.vert"
        is OrthoProjectionMode -> "!shader/plugin/vprojection.ortho.vert"
        is LogProjectionMode -> "!shader/plugin/vprojection.log.vert"
        else -> ""
    }
}

internal class ModelModifier(
    val model: Mat4,
) : InternalMaterialModifier(
    "model" to Mat4Getter<ModelModifier> { it.model },
)

internal class TimeMaterialModifier(nodeContext: NodeContext, renderContext: RenderContext) : InternalMaterialModifier(
    "time" to FloatGetter<TimeMaterialModifier> {
        it.time
    }
) {
    val time = nodeContext.time ?: renderContext.time
}
