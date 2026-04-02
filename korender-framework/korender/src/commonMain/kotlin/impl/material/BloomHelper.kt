package com.zakgof.korender.impl.material

import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.impl.engine.FrameTarget
import com.zakgof.korender.impl.engine.InternalPassDeclaration
import com.zakgof.korender.impl.glgpu.FloatGetter

internal fun bloomMipEffect(
    currentRetentionPolicy: RetentionPolicy,
    threshold: Float, amount: Float, downsample: Int, mips: Int, offset: Float, highResolutionRatio: Float,
) = InternalPostShadingEffect(
    effectPasses = listOf(bloomBrightnessPass(currentRetentionPolicy, downsample, threshold)) +
            bloomDownsamplePasses(currentRetentionPolicy, downsample, mips, offset) +
            bloomUpsamplePasses(currentRetentionPolicy, downsample, mips, offset, highResolutionRatio),
    keepTextures = setOf("bloomTexture", "bloomDepth"),
    compositionMaterialModifier = BloomCompositionMaterialModifier(amount),
    currentRetentionPolicy
)

internal fun bloomSimpleEffect(
    currentRetentionPolicy: RetentionPolicy,
    threshold: Float, amount: Float, radius: Float, downsample: Int,
) = InternalPostShadingEffect(
    effectPasses = listOf(
        bloomBrightnessPass(currentRetentionPolicy, downsample, threshold),
        bloomVerticalBlur(currentRetentionPolicy, downsample, radius),
        bloomHorizontalBlur(currentRetentionPolicy, downsample, radius)
    ),
    keepTextures = setOf("bloomTexture", "bloomDepth"),
    compositionMaterialModifier = BloomCompositionMaterialModifier(amount),
    currentRetentionPolicy
)

private fun bloomBrightnessPass(currentRetentionPolicy: RetentionPolicy, brightnessDownsample: Int, threshold: Float) =
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
        currentRetentionPolicy
    )

private fun bloomDownsamplePasses(
    currentRetentionPolicy: RetentionPolicy,
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
        currentRetentionPolicy
    )
}

private fun bloomUpsamplePasses(
    currentRetentionPolicy: RetentionPolicy,
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
        currentRetentionPolicy
    )
}

private fun bloomVerticalBlur(currentRetentionPolicy: RetentionPolicy, downsample: Int, radius: Float) =
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
        currentRetentionPolicy
    )

private fun bloomHorizontalBlur(currentRetentionPolicy: RetentionPolicy, downsample: Int, radius: Float) =
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
        currentRetentionPolicy
    )

private class KawaseMaterial(
    val offset: Float,
    val highResolutionRatio: Float?,
) : InternalPostProcessingMaterial(
    "!shader/effect/kawase.frag",
    "offset" to FloatGetter<KawaseMaterial> { it.offset },
    "highResolutionRatio" to FloatGetter<KawaseMaterial> { it.highResolutionRatio }
) {
    override fun collectDefs(accumulator: MutableSet<String>) {
        super.collectDefs(accumulator)
        highResolutionRatio?.let { accumulator += "UPSAMPLE" }
    }
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

    override fun collectDefs(accumulator: MutableSet<String>) {
        super.collectDefs(accumulator)
        accumulator += "BLOOM"
    }
}
