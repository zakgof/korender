package com.zakgof.korender.impl.material

import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.impl.engine.FrameTarget
import com.zakgof.korender.impl.engine.InternalPassDeclaration
import com.zakgof.korender.impl.engine.RenderContext
import com.zakgof.korender.impl.glgpu.FloatGetter

internal fun bloomMipEffect(
    renderContext: RenderContext, currentRetentionPolicy: RetentionPolicy,
    threshold: Float, amount: Float, downsample: Int, mips: Int, offset: Float, highResolutionRatio: Float,
) = InternalPostShadingEffect(
    effectPasses = listOf(bloomBrightnessPass(renderContext, currentRetentionPolicy, downsample, threshold)) +
            bloomDownsamplePasses(renderContext, currentRetentionPolicy, downsample, mips, offset) +
            bloomUpsamplePasses(renderContext, currentRetentionPolicy, downsample, mips, offset, highResolutionRatio),
    keepTextures = setOf("bloomTexture", "bloomDepth"),
    compositionMaterialModifier = BloomCompositionMaterialModifier(amount),
    currentRetentionPolicy
)

internal fun bloomSimpleEffect(
    renderContext: RenderContext, currentRetentionPolicy: RetentionPolicy,
    threshold: Float, amount: Float, radius: Float, downsample: Int,
) = InternalPostShadingEffect(
    effectPasses = listOf(
        bloomBrightnessPass(renderContext, currentRetentionPolicy, downsample, threshold),
        bloomVerticalBlur(renderContext, currentRetentionPolicy, downsample, radius),
        bloomHorizontalBlur(renderContext, currentRetentionPolicy, downsample, radius)
    ),
    keepTextures = setOf("bloomTexture", "bloomDepth"),
    compositionMaterialModifier = BloomCompositionMaterialModifier(amount),
    currentRetentionPolicy
)

private fun bloomBrightnessPass(renderContext: RenderContext, currentRetentionPolicy: RetentionPolicy, brightnessDownsample: Int, threshold: Float) =
    InternalPassDeclaration(
        mapOf(
            "colorInputTexture" to "colorTexture",
            "depthInputTexture" to "depthTexture"
        ),
        BloomMaterial(threshold),
        null,
        FrameTarget(
            renderContext.width / brightnessDownsample,
            renderContext.height / brightnessDownsample,
            "downsample0",
            "bloomDepth"
        ),
        currentRetentionPolicy
    )

private fun bloomDownsamplePasses(
    renderContext: RenderContext,
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
            renderContext.width / (brightnessDownsample shl pass),
            renderContext.height / (brightnessDownsample shl pass),
            "downsample${pass}",
            "bloomDepth"
        ),
        currentRetentionPolicy
    )
}

private fun bloomUpsamplePasses(
    renderContext: RenderContext,
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
            renderContext.width / (brightnessDownsample shl pass),
            renderContext.height / (brightnessDownsample shl pass),
            (if (pass == 1) "bloomTexture" else "upsample${pass - 1}"),
            "bloomDepth"
        ),
        currentRetentionPolicy
    )
}

private fun bloomVerticalBlur(renderContext: RenderContext, currentRetentionPolicy: RetentionPolicy, downsample: Int, radius: Float) =
    InternalPassDeclaration(
        mapOf(
            "colorInputTexture" to "downsample0",
            "depthInputTexture" to "bloomDepth"
        ),
        BlurMaterial(true, radius),
        null,
        FrameTarget(
            renderContext.width / downsample,
            renderContext.height / downsample,
            "downsample1",
            "bloomDepth"
        ),
        currentRetentionPolicy
    )

private fun bloomHorizontalBlur(renderContext: RenderContext, currentRetentionPolicy: RetentionPolicy, downsample: Int, radius: Float) =
    InternalPassDeclaration(
        mapOf(
            "colorInputTexture" to "downsample1",
            "depthInputTexture" to "bloomDepth"
        ),
        BlurMaterial(false, radius),
        null,
        FrameTarget(
            renderContext.width / downsample,
            renderContext.height / downsample,
            "bloomTexture",
            "bloomDepth"
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
    override val defs
        get() = super.defs + setOfNotNull(highResolutionRatio?.let { "UPSAMPLE" })
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
    override val defs
        get() = super.defs + setOf("BLOOM")
}
