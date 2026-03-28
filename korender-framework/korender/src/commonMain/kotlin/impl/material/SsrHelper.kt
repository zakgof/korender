package com.zakgof.korender.impl.material

import com.zakgof.korender.CubeTextureDeclaration
import com.zakgof.korender.PostShadingEffect
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.impl.engine.FrameTarget
import com.zakgof.korender.impl.engine.InternalPassDeclaration
import com.zakgof.korender.impl.engine.RenderContext
import com.zakgof.korender.impl.glgpu.FloatGetter
import com.zakgof.korender.impl.glgpu.IntGetter
import com.zakgof.korender.impl.glgpu.TextureGetter
import kotlin.math.pow

internal fun ssrEffect(
    downsample: Int,
    maxReflectionDistance: Float,
    linearSteps: Int, binarySteps: Int,
    lastStepRatio: Float,
    envTexture: CubeTextureDeclaration?,
    renderContext: RenderContext,
    currentRetentionPolicy: RetentionPolicy,
): PostShadingEffect {
    val nextStepRatio = lastStepRatio.pow(1f / (linearSteps + 1f))
    return InternalPostShadingEffect(
        effectPasses = listOf(
            InternalPassDeclaration(
                mapOf("colorInputTexture" to "colorTexture"),
                SsrMaterial(
                    linearSteps, binarySteps, maxReflectionDistance,
                    nextStepRatio,
                    maxReflectionDistance * (1f - nextStepRatio) / (1f - nextStepRatio.pow(linearSteps))
                ),
                null,
                FrameTarget(renderContext.width / downsample, renderContext.height / downsample, "ssrTexture", "ssrDepth"),
                currentRetentionPolicy
            )
        ),
        keepTextures = setOf("ssrTexture"),
        compositionMaterialModifier = SsrCompositionMaterialModifier(envTexture),
        currentRetentionPolicy
    )
}

private class SsrMaterial(
    val linearSteps: Int,
    val binarySteps: Int,
    val maxReflectionDistance: Float,
    val nextStepRatio: Float,
    val startStep: Float,
) : InternalPostProcessingMaterial(
    "!shader/effect/ssr.frag",
    "linearSteps" to IntGetter<SsrMaterial> { it.linearSteps },
    "binarySteps" to IntGetter<SsrMaterial> { it.binarySteps },
    "maxReflectionDistance" to FloatGetter<SsrMaterial> { it.maxReflectionDistance },
    "nextStepRatio" to FloatGetter<SsrMaterial> { it.nextStepRatio },
    "startStep" to FloatGetter<SsrMaterial> { it.startStep }
)

private class SsrCompositionMaterialModifier(val envTexture: CubeTextureDeclaration?) : InternalMaterialModifier(
    "envTexture" to TextureGetter<SsrCompositionMaterialModifier> { it.envTexture }
) {
    override val defs
        get() = super.defs + "BLOOM"
}

