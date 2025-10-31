package com.zakgof.korender.impl.engine

import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.impl.material.InternalMaterialModifier
import com.zakgof.korender.impl.material.InternalPostShadingEffect

internal fun bloomEffect(renderContext: RenderContext, currentRetentionPolicy: RetentionPolicy, brightnessDownsample: Int, passes: Int) = InternalPostShadingEffect(
    "bloom2",
    effectPasses = listOf(bloomBrightnessPass(renderContext, currentRetentionPolicy, brightnessDownsample)) +
            bloomDownsamplePasses(renderContext, currentRetentionPolicy, brightnessDownsample, passes) +
            bloomUpsamplePasses(renderContext, currentRetentionPolicy, brightnessDownsample, passes),
    compositionMaterialModifier = {
        it.shaderDefs += "BLOOM"
    }, currentRetentionPolicy
)

private fun bloomBrightnessPass(renderContext: RenderContext, currentRetentionPolicy: RetentionPolicy, brightnessDownsample: Int) =
    InternalPassDeclaration(
        mapOf("colorInputTexture" to "colorTexture"),
        listOf(
            InternalMaterialModifier {
                it.fragShaderFile = "!shader/effect/bloom.frag"
                it.uniforms["threshold"] = 0.8f
            }),
        null,
        FrameTarget(
            renderContext.width / brightnessDownsample,
            renderContext.height / brightnessDownsample,
            "downsample0",
            "dummy"
        ),
        currentRetentionPolicy
    )

private fun bloomDownsamplePasses(
    renderContext: RenderContext,
    currentRetentionPolicy: RetentionPolicy,
    brightnessDownsample: Int,
    passes: Int
) = (1 .. passes).map { pass ->
    InternalPassDeclaration(
        mapOf("colorInputTexture" to "downsample${pass-1}"),
        listOf(
            InternalMaterialModifier {
                it.fragShaderFile = "!shader/effect/kawase.frag"
            }),
        null,
        FrameTarget(
            renderContext.width / (brightnessDownsample shl pass),
            renderContext.height / (brightnessDownsample shl pass),
            "downsample${pass}",
            "dummy"
        ),
        currentRetentionPolicy
    )
}

private fun bloomUpsamplePasses(
    renderContext: RenderContext,
    currentRetentionPolicy: RetentionPolicy,
    brightnessDownsample: Int,
    passes: Int
) = (passes downTo 1).map { pass ->
    InternalPassDeclaration(
        mapOf(
            "lowResTexture" to if (pass == passes) "downsample${pass}" else "upsample${pass}",
            "highResTexture" to "downsample${pass-1}"

        ),
        listOf(
            InternalMaterialModifier {
                it.fragShaderFile = "!shader/effect/kawase-upsample.frag"
            }),
        null,
        FrameTarget(
            renderContext.width / (brightnessDownsample shl pass),
            renderContext.height / (brightnessDownsample shl pass),
            (if (pass == 1) "bloomTexture" else "upsample${pass-1}"),
            "dummy"
        ),
        currentRetentionPolicy
    )
}
