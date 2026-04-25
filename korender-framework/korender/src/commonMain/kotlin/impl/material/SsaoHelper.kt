package com.zakgof.korender.impl.material

import com.zakgof.korender.impl.context.NodeContext
import com.zakgof.korender.impl.engine.FrameTarget
import com.zakgof.korender.impl.engine.ResultKeeper
import com.zakgof.korender.impl.engine.ShaderDeclaration
import com.zakgof.korender.impl.engine.SsaoDeclaration
import com.zakgof.korender.impl.glgpu.FloatGetter
import com.zakgof.korender.impl.glgpu.IntGetter
import com.zakgof.korender.impl.glgpu.Vec2Getter
import com.zakgof.korender.math.Vec2

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

internal fun renderSsao(
    nodeContext: NodeContext,
    frameWidth: Int,
    frameHeight: Int,
    materialModifier: InternalMaterialModifier,
    ssaoDeclaration: SsaoDeclaration,
    rk: ResultKeeper?,
    renderFullscreen: (ShaderDeclaration, Int, Int, ResultKeeper?) -> Unit,
    renderToReusableFb: (FrameTarget, ResultKeeper?, () -> Unit) -> Unit,
    unlock: (String) -> Unit,
) {
    val downsample = ssaoDeclaration.downsample.coerceAtLeast(1)
    val ssaoWidth = maxOf(1, frameWidth / downsample)
    val ssaoHeight = maxOf(1, frameHeight / downsample)

    try {
        val ssaoMaterialDeclaration = SsaoMaterial(
            ssaoDeclaration.sampleCount,
            ssaoDeclaration.radius,
            ssaoDeclaration.bias,
            ssaoDeclaration.intensity
        ).toDeclaration(true, nodeContext, arrayOf(materialModifier, null, null))
        renderToReusableFb(FrameTarget("ssaoRawTexture", "ssaoRawDepth", downsample), rk) {
            renderFullscreen(ssaoMaterialDeclaration, ssaoWidth, ssaoHeight, rk)
        }

        val ssaoBlurVDeclaration = SsaoBlurMaterial(Vec2(0f, 1f), downsample, ssaoDeclaration.blurRadius).toDeclaration(
            true,
            nodeContext,
            arrayOf(materialModifier, null, null)
        )
        materialModifier.customTextureUniforms["aoInputTexture"] = materialModifier.customTextureUniforms["ssaoRawTexture"]!!
        materialModifier.customTextureUniforms["depthInputTexture"] = materialModifier.customTextureUniforms["depthGeometryTexture"]!!
        renderToReusableFb(FrameTarget("ssaoBlurTexture", "ssaoBlurDepth", downsample), rk) {
            renderFullscreen(ssaoBlurVDeclaration, ssaoWidth, ssaoHeight, rk)
        }

        val ssaoBlurHDeclaration = SsaoBlurMaterial(Vec2(1f, 0f), downsample, ssaoDeclaration.blurRadius).toDeclaration(
            true,
            nodeContext,
            arrayOf(materialModifier, null, null)
        )
        materialModifier.customTextureUniforms["aoInputTexture"] = materialModifier.customTextureUniforms["ssaoBlurTexture"]!!
        renderToReusableFb(FrameTarget("ssaoTexture", "ssaoDepth", downsample), rk) {
            renderFullscreen(ssaoBlurHDeclaration, ssaoWidth, ssaoHeight, rk)
        }
    } finally {
        unlock("ssaoRawTexture")
        unlock("ssaoRawDepth")
        unlock("ssaoBlurTexture")
        unlock("ssaoBlurDepth")
    }
}
