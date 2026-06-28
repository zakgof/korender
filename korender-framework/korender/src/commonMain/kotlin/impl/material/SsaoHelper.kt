package com.zakgof.korender.impl.material

import com.zakgof.korender.impl.context.NodeContext
import com.zakgof.korender.impl.engine.FrameTarget
import com.zakgof.korender.impl.engine.InternalPassDeclaration
import com.zakgof.korender.impl.glgpu.FloatGetter
import com.zakgof.korender.impl.glgpu.IntGetter
import com.zakgof.korender.impl.glgpu.Vec2Getter
import com.zakgof.korender.math.Vec2

internal fun ssaoEffect(
    downsample: Int, sampleCount: Int, radius: Float, bias: Float, intensity: Float, blurRadius: Float,
    nodeContext: NodeContext,
) = InternalMultiPassEffect(
    effectPasses = listOf(
        ssaoPass(nodeContext, downsample, sampleCount, radius, bias, intensity),
        ssaoBlur(nodeContext, downsample, blurRadius, Vec2(0f, 1f), "ssaoRawTexture", "aoInputTexture"),
        ssaoBlur(nodeContext, downsample, blurRadius, Vec2(1f, 0f), "aoInputTexture", "ssaoTexture")
    ),
    keepTextures = setOf("ssaoRawTexture"),
    finalMaterialModifier = SsaoShadingMaterialModifier,
    nodeContext
)

private fun ssaoPass(nodeContext: NodeContext, downsample: Int, sampleCount: Int, radius: Float, bias: Float, intensity: Float): InternalPassDeclaration =
    InternalPassDeclaration(
        mapOf(),
        SsaoMaterial(sampleCount, radius, bias, intensity),
        null,
        FrameTarget(
            "ssaoRawTexture",
            "dummy",
            downsample
        ),
        nodeContext
    )

private fun ssaoBlur(nodeContext: NodeContext, downsample: Int, blurRadius: Float, direction: Vec2, input: String, output: String) =
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

internal class SsaoMaterial(
    val sampleCount: Int,
    val radius: Float,
    val bias: Float,
    val intensity: Float,
) : InternalPostProcessingMaterial(
    "!shader/effect/ssao.frag",
    "sampleCount" to IntGetter<SsaoMaterial> { it.sampleCount },
    "radius" to FloatGetter<SsaoMaterial> { it.radius },
    "bias" to FloatGetter<SsaoMaterial> { it.bias },
    "intensity" to FloatGetter<SsaoMaterial> { it.intensity },
)

internal class SsaoBlurMaterial(
    val direction: Vec2,
    val downsample: Int,
    val radius: Float,
) : InternalPostProcessingMaterial(
    "!shader/effect/ssao_blur.frag",
    "direction" to Vec2Getter<SsaoBlurMaterial> { it.direction },
    "downsample" to IntGetter<SsaoBlurMaterial> { it.downsample },
    "radius" to FloatGetter<SsaoBlurMaterial> { it.radius },
)

internal object SsaoShadingMaterialModifier : InternalMaterialModifier(
) {
    override fun collectDefs(accumulator: Long) = accumulator or Defs.SSAO.bit
}