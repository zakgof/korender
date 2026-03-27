package com.zakgof.korender.impl.material

import com.zakgof.korender.impl.engine.Engine
import com.zakgof.korender.impl.engine.InternalFilterDeclaration
import com.zakgof.korender.impl.engine.InternalPassDeclaration
import com.zakgof.korender.impl.engine.RenderContext
import com.zakgof.korender.impl.engine.SceneDeclaration
import com.zakgof.korender.impl.glgpu.FloatGetter

internal class BlurMaterial(
    vertical: Boolean,
    val radius: Float,
) : InternalPostProcessingMaterial(
    if (vertical) "!shader/effect/blurv.frag" else "!shader/effect/blurh.frag",
    "radius" to FloatGetter<BlurMaterial> { it.radius }
)

internal fun Engine.KorenderContextImpl.simpleBlur(renderContext: RenderContext, radius: Float) = InternalFilterDeclaration(
    listOf(
        InternalPassDeclaration(
            mapOf(
                "colorInputTexture" to "colorTexture",
                "depthInputTexture" to "depthTexture"
            ),
            material = blurVert(radius),
            retentionPolicy = currentRetentionPolicy,
            sceneDeclaration = SceneDeclaration(),
            target = renderContext.defaultTarget()
        ),
        InternalPassDeclaration(
            mapOf(
                "colorInputTexture" to "colorTexture",
                "depthInputTexture" to "depthTexture"
            ),
            material = blurHorz(radius),
            retentionPolicy = currentRetentionPolicy,
            sceneDeclaration = SceneDeclaration(),
            target = renderContext.defaultTarget()
        )
    )
)

internal class AdjustmentMaterial(
    val brightness: Float,
    val contrast: Float,
    val saturation: Float
) : InternalPostProcessingMaterial(
    "!shader/effect/adjust.frag",
    "brightness" to FloatGetter<AdjustmentMaterial> { it.brightness },
    "contrast" to FloatGetter<AdjustmentMaterial> { it.contrast },
    "saturation" to FloatGetter<AdjustmentMaterial> { it.saturation }
)