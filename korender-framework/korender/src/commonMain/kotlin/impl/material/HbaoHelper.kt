package com.zakgof.korender.impl.material

import com.zakgof.korender.impl.context.NodeContext
import com.zakgof.korender.impl.engine.FrameTarget
import com.zakgof.korender.impl.engine.InternalPassDeclaration
import com.zakgof.korender.impl.glgpu.FloatGetter
import com.zakgof.korender.impl.glgpu.IntGetter
import com.zakgof.korender.math.Vec2

internal fun hbaoEffect(
    downsample: Int,
    sampleCount: Int,
    radius: Float,
    bias: Float,
    intensity: Float,
    blurRadius: Float,
    nodeContext: NodeContext,
) = InternalMultiPassEffect(
    effectPasses = listOf(
        hbaoPass(nodeContext, downsample, sampleCount, radius, bias, intensity),
        hbaoBlur(nodeContext, downsample, blurRadius, Vec2(0f, 1f), "hbaoRawTexture", "aoInputTexture"),
        hbaoBlur(nodeContext, downsample, blurRadius, Vec2(1f, 0f), "aoInputTexture", "hbaoTexture")
    ),
    keepTextures = setOf("hbaoRawTexture"),
    finalMaterialModifier = HbaoShadingMaterialModifier,
    nodeContext
)

private fun hbaoPass(
    nodeContext: NodeContext,
    downsample: Int,
    sampleCount: Int,
    radius: Float,
    bias: Float,
    intensity: Float,
): InternalPassDeclaration =
    InternalPassDeclaration(
        mapOf(),
        HbaoMaterial(sampleCount, radius, bias, intensity),
        null,
        FrameTarget(
            "hbaoRawTexture",
            "dummy",
            downsample
        ),
        nodeContext
    )

private fun hbaoBlur(nodeContext: NodeContext, downsample: Int, blurRadius: Float, direction: Vec2, input: String, output: String) =
    InternalPassDeclaration(
        mapOf("aoInputTexture" to input),
        SsaoBlurMaterial(direction, downsample, blurRadius),
        null,
        FrameTarget(
            output,
            "dummy",
            downsample
        ),
        nodeContext
    )

internal class HbaoMaterial(
    val sampleCount: Int,
    val radius: Float,
    val bias: Float,
    val intensity: Float,
) : InternalPostProcessingMaterial(
    "!shader/effect/hbao.frag",
    "sampleCount" to IntGetter<HbaoMaterial> { it.sampleCount },
    "radius" to FloatGetter<HbaoMaterial> { it.radius },
    "bias" to FloatGetter<HbaoMaterial> { it.bias },
    "intensity" to FloatGetter<HbaoMaterial> { it.intensity },
)

internal object HbaoShadingMaterialModifier : InternalMaterialModifier() {
    override fun collectDefs(accumulator: Long) = accumulator or Defs.HBAO.bit
}
