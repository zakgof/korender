package com.zakgof.korender.impl.engine

import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.impl.material.InternalMaterialModifier
import com.zakgof.korender.impl.material.InternalPostShadingEffect

internal fun bloomMipEffect(
    renderContext: RenderContext, currentRetentionPolicy: RetentionPolicy,
    threshold: Float, amount: Float, downsample: Int, mips: Int, offset: Float, highResolutionRatio: Float,
) = InternalPostShadingEffect(
    "bloom2",
    effectPasses = listOf(bloomBrightnessPass(renderContext, currentRetentionPolicy, downsample, threshold)) +
            bloomDownsamplePasses(renderContext, currentRetentionPolicy, downsample, mips, offset) +
            bloomUpsamplePasses(renderContext, currentRetentionPolicy, downsample, mips, offset, highResolutionRatio),
    compositionMaterialModifier = {
        it.shaderDefs += "BLOOM"
        it.uniforms["bloomAmount"] = amount
    }, currentRetentionPolicy
)

internal fun bloomSimpleEffect(
    renderContext: RenderContext, currentRetentionPolicy: RetentionPolicy,
    threshold: Float, amount: Float, radius: Float, downsample: Int,
) = InternalPostShadingEffect(
    "bloom",
    effectPasses = listOf(
        bloomBrightnessPass(renderContext, currentRetentionPolicy, downsample, threshold),
        bloomVerticalBlur(renderContext, currentRetentionPolicy, downsample, radius),
        bloomHorizontalBlur(renderContext, currentRetentionPolicy, downsample, radius)
    ),
    compositionMaterialModifier = {
        it.shaderDefs += "BLOOM"
        it.uniforms["bloomAmount"] = amount
    }, currentRetentionPolicy
)

private fun bloomBrightnessPass(renderContext: RenderContext, currentRetentionPolicy: RetentionPolicy, brightnessDownsample: Int, threshold: Float) =
    InternalPassDeclaration(
        mapOf(
            "colorInputTexture" to "colorTexture",
            "depthInputTexture" to "depthTexture"
        ),
        listOf(
            InternalMaterialModifier {
                it.fragShaderFile = "!shader/effect/bloom.frag"
                it.uniforms["threshold"] = threshold
            }),
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
        listOf(
            InternalMaterialModifier {
                it.fragShaderFile = "!shader/effect/kawase.frag"
                it.uniforms["offset"] = offset
            }),
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
        listOf(
            InternalMaterialModifier {
                it.fragShaderFile = "!shader/effect/kawase.frag"
                it.uniforms["offset"] = offset
                it.uniforms["highResolutionRatio"] = highResolutionRatio
                it.shaderDefs += "UPSAMPLE"
            }),
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
        listOf(
            InternalMaterialModifier {
                it.fragShaderFile = "!shader/effect/blurv.frag"
                it.uniforms["radius"] = radius
            }),
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
        listOf(
            InternalMaterialModifier {
                it.fragShaderFile = "!shader/effect/blurh.frag"
                it.uniforms["radius"] = radius
            }),
        null,
        FrameTarget(
            renderContext.width / downsample,
            renderContext.height / downsample,
            "bloomTexture",
            "bloomDepth"
        ),
        currentRetentionPolicy
    )

