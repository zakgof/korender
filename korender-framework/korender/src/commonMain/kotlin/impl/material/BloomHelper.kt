package com.zakgof.korender.impl.material

import com.zakgof.korender.impl.context.NodeContext
import com.zakgof.korender.impl.engine.FrameTarget
import com.zakgof.korender.impl.engine.InternalPassDeclaration
import com.zakgof.korender.impl.glgpu.FloatGetter

internal fun bloomMipEffect(
    threshold: Float, amount: Float, downsample: Int, mips: Int, offset: Float, highResolutionRatio: Float, nodeContext: NodeContext,
) = InternalPostShadingEffect(
    effectPasses = listOf(bloomBrightnessPass(nodeContext, downsample, threshold)) +
            bloomDownsamplePasses(nodeContext, downsample, mips, offset) +
            bloomUpsamplePasses(nodeContext, downsample, mips, offset, highResolutionRatio),
    keepTextures = setOf("bloomTexture", "bloomDepth"),
    compositionMaterialModifier = BloomCompositionMaterialModifier(amount),
    nodeContext
)

internal fun bloomSimpleEffect(
    threshold: Float, amount: Float, radius: Float, downsample: Int, nodeContext: NodeContext,
) = InternalPostShadingEffect(
    effectPasses = listOf(
        bloomBrightnessPass(nodeContext, downsample, threshold),
        bloomVerticalBlur(nodeContext, downsample, radius),
        bloomHorizontalBlur(nodeContext, downsample, radius)
    ),
    keepTextures = setOf("bloomTexture", "bloomDepth"),
    compositionMaterialModifier = BloomCompositionMaterialModifier(amount),
    nodeContext
)

private fun bloomBrightnessPass(nodeContext: NodeContext, brightnessDownsample: Int, threshold: Float) =
    InternalPassDeclaration(
        mapOf(
            "colorInputTexture" to "colorTexture",
            "depthInputTexture" to "depthTexture"
        ),
        BloomMaterial(threshold),
        null,
        FrameTarget(
            "downsample0",
            "bloomDepth",
            brightnessDownsample
        ),
        nodeContext
    )

private fun bloomDownsamplePasses(
    nodeContext: NodeContext,
    brightnessDownsample: Int,
    passes: Int,
    offset: Float,
) = (1..passes).map { pass ->
    InternalPassDeclaration(
        mapOf(
            "colorInputTexture" to "downsample${pass - 1}",
            "depthInputTexture" to "bloomDepth"
        ),
        KawaseMaterial(offset, null),
        null,
        FrameTarget(
            "downsample${pass}",
            "bloomDepth",
            brightnessDownsample shl pass
        ),
        nodeContext
    )
}

private fun bloomUpsamplePasses(
    nodeContext: NodeContext,
    brightnessDownsample: Int,
    passes: Int,
    offset: Float,
    highResolutionRatio: Float,
) = (passes downTo 1).map { pass ->
    InternalPassDeclaration(
        mapOf(
            "colorInputTexture" to if (pass == passes) "downsample${pass}" else "upsample${pass}",
            "highResTexture" to "downsample${pass - 1}",
            "depthInputTexture" to "bloomDepth"
        ),
        KawaseMaterial(offset, highResolutionRatio),
        null,
        FrameTarget(
            (if (pass == 1) "bloomTexture" else "upsample${pass - 1}"),
            "bloomDepth",
            brightnessDownsample shl pass
        ),
        nodeContext
    )
}

private fun bloomVerticalBlur(nodeContext: NodeContext, downsample: Int, radius: Float) =
    InternalPassDeclaration(
        mapOf(
            "colorInputTexture" to "downsample0",
            "depthInputTexture" to "bloomDepth"
        ),
        BlurMaterial(true, radius),
        null,
        FrameTarget(
            "downsample1",
            "bloomDepth",
            downsample
        ),
        nodeContext
    )

private fun bloomHorizontalBlur(nodeContext: NodeContext, downsample: Int, radius: Float) =
    InternalPassDeclaration(
        mapOf(
            "colorInputTexture" to "downsample1",
            "depthInputTexture" to "bloomDepth"
        ),
        BlurMaterial(false, radius),
        null,
        FrameTarget(
            "bloomTexture",
            "bloomDepth",
            downsample
        ),
        nodeContext
    )

private class KawaseMaterial(
    val offset: Float,
    val highResolutionRatio: Float?,
) : InternalPostProcessingMaterial(
    "!shader/effect/kawase.frag",
    "offset" to FloatGetter<KawaseMaterial> { it.offset },
    "highResolutionRatio" to FloatGetter<KawaseMaterial> { it.highResolutionRatio }
) {
    override fun collectDefs(accumulator: Long) = super.collectDefs(accumulator)
        .combineDefsIfNotNull(highResolutionRatio, Defs.UPSAMPLE)
}

private class BloomMaterial(
    val threshold: Float,
) : InternalPostProcessingMaterial(
    "!shader/effect/bloom.frag",
    "threshold" to FloatGetter<BloomMaterial> { it.threshold },
)


private class BloomCompositionMaterialModifier(val bloomAmount: Float) : InternalMaterialModifier(
    "bloomAmount" to FloatGetter<BloomCompositionMaterialModifier> { it.bloomAmount }
) {

    override fun collectDefs(accumulator: Long): Long =
        super.collectDefs(accumulator) or Defs.BLOOM.bit
}
