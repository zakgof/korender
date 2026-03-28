package com.zakgof.korender.impl.material

import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.impl.glgpu.FloatGetter

internal fun bloomMipEffect(
    renderContext: com.zakgof.korender.impl.engine.RenderContext, currentRetentionPolicy: RetentionPolicy,
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
    renderContext: com.zakgof.korender.impl.engine.RenderContext, currentRetentionPolicy: RetentionPolicy,
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

private fun bloomBrightnessPass(renderContext: com.zakgof.korender.impl.engine.RenderContext, currentRetentionPolicy: RetentionPolicy, brightnessDownsample: Int, threshold: Float) =
    _root_ide_package_.com.zakgof.korender.impl.engine.InternalPassDeclaration(
        mapOf(
            "colorInputTexture" to "colorTexture",
            "depthInputTexture" to "depthTexture"
        ),
        BloomMaterial(threshold),
        null,
        _root_ide_package_.com.zakgof.korender.impl.engine.FrameTarget(
            renderContext.width / brightnessDownsample,
            renderContext.height / brightnessDownsample,
            "downsample0",
            "bloomDepth"
        ),
        currentRetentionPolicy
    )

private fun bloomDownsamplePasses(
    renderContext: com.zakgof.korender.impl.engine.RenderContext,
    currentRetentionPolicy: RetentionPolicy,
    brightnessDownsample: Int,
    passes: Int,
    offset: Float,
) = (1..passes).map { pass ->
    _root_ide_package_.com.zakgof.korender.impl.engine.InternalPassDeclaration(
        mapOf(
            "colorInputTexture" to "downsample${pass - 1}",
            "depthInputTexture" to "bloomDepth"
        ),
        KawaseMaterial(offset, null),
        null,
        _root_ide_package_.com.zakgof.korender.impl.engine.FrameTarget(
            renderContext.width / (brightnessDownsample shl pass),
            renderContext.height / (brightnessDownsample shl pass),
            "downsample${pass}",
            "bloomDepth"
        ),
        currentRetentionPolicy
    )
}

private fun bloomUpsamplePasses(
    renderContext: com.zakgof.korender.impl.engine.RenderContext,
    currentRetentionPolicy: RetentionPolicy,
    brightnessDownsample: Int,
    passes: Int,
    offset: Float,
    highResolutionRatio: Float,
) = (passes downTo 1).map { pass ->
    _root_ide_package_.com.zakgof.korender.impl.engine.InternalPassDeclaration(
        mapOf(
            "colorInputTexture" to if (pass == passes) "downsample${pass}" else "upsample${pass}",
            "highResTexture" to "downsample${pass - 1}",
            "depthInputTexture" to "bloomDepth"
        ),
        KawaseMaterial(offset, highResolutionRatio),
        null,
        _root_ide_package_.com.zakgof.korender.impl.engine.FrameTarget(
            renderContext.width / (brightnessDownsample shl pass),
            renderContext.height / (brightnessDownsample shl pass),
            (if (pass == 1) "bloomTexture" else "upsample${pass - 1}"),
            "bloomDepth"
        ),
        currentRetentionPolicy
    )
}

private fun bloomVerticalBlur(renderContext: com.zakgof.korender.impl.engine.RenderContext, currentRetentionPolicy: RetentionPolicy, downsample: Int, radius: Float) =
    _root_ide_package_.com.zakgof.korender.impl.engine.InternalPassDeclaration(
        mapOf(
            "colorInputTexture" to "downsample0",
            "depthInputTexture" to "bloomDepth"
        ),
        BlurMaterial(true, radius),
        null,
        _root_ide_package_.com.zakgof.korender.impl.engine.FrameTarget(
            renderContext.width / downsample,
            renderContext.height / downsample,
            "downsample1",
            "bloomDepth"
        ),
        currentRetentionPolicy
    )

private fun bloomHorizontalBlur(renderContext: com.zakgof.korender.impl.engine.RenderContext, currentRetentionPolicy: RetentionPolicy, downsample: Int, radius: Float) =
    _root_ide_package_.com.zakgof.korender.impl.engine.InternalPassDeclaration(
        mapOf(
            "colorInputTexture" to "downsample1",
            "depthInputTexture" to "bloomDepth"
        ),
        BlurMaterial(false, radius),
        null,
        _root_ide_package_.com.zakgof.korender.impl.engine.FrameTarget(
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
    override val defs: Set<String>
        get() = setOfNotNull(highResolutionRatio?.let { "UPSAMPLE" })
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
    override val defs: Set<String>
        get() = setOf("BLOOM")
}
