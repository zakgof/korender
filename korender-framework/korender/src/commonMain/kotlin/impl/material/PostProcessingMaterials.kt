package com.zakgof.korender.impl.material

import com.zakgof.korender.impl.context.NodeContext
import com.zakgof.korender.impl.engine.FrameTarget
import com.zakgof.korender.impl.engine.InternalFilterDeclaration
import com.zakgof.korender.impl.engine.InternalPassDeclaration
import com.zakgof.korender.impl.engine.NodeKeeper
import com.zakgof.korender.impl.engine.SceneDeclaration
import com.zakgof.korender.impl.glgpu.ColorRGBGetter
import com.zakgof.korender.impl.glgpu.CompositeSupplier
import com.zakgof.korender.impl.glgpu.FloatGetter
import com.zakgof.korender.math.ColorRGB

internal class BlurMaterial(
    vertical: Boolean,
    val radius: Float,
) : InternalPostProcessingMaterial(
    if (vertical) "!shader/effect/blurv.frag" else "!shader/effect/blurh.frag",
    "radius" to FloatGetter<BlurMaterial> { it.radius }
)

internal fun simpleBlur(radius: Float, nodeContext: NodeContext) = InternalFilterDeclaration(
    listOf(
        InternalPassDeclaration(
            mapOf(
                "colorInputTexture" to "colorTexture",
                "depthInputTexture" to "depthTexture"
            ),
            material = BlurMaterial(true, radius),
            nodeContext = nodeContext,
            sceneDeclaration = SceneDeclaration(),
            target = FrameTarget.default
        ),
        InternalPassDeclaration(
            mapOf(
                "colorInputTexture" to "colorTexture",
                "depthInputTexture" to "depthTexture"
            ),
            material = BlurMaterial(false, radius),
            nodeContext = nodeContext,
            sceneDeclaration = SceneDeclaration(),
            target = FrameTarget.default
        )
    )
)

internal class AdjustmentMaterial(
    val brightness: Float,
    val contrast: Float,
    val saturation: Float,
) : InternalPostProcessingMaterial(
    "!shader/effect/adjust.frag",
    "brightness" to FloatGetter<AdjustmentMaterial> { it.brightness },
    "contrast" to FloatGetter<AdjustmentMaterial> { it.contrast },
    "saturation" to FloatGetter<AdjustmentMaterial> { it.saturation }
)

internal class WaterMaterial(
    val waterColor: ColorRGB,
    val transparency: Float,
    val waveScale: Float,
    val waveMagnitude: Float,
    val sky: InternalSkyMaterial,
) : InternalPostProcessingMaterial(
    "!shader/effect/water.frag",
    "waterColor" to ColorRGBGetter<WaterMaterial> { it.waterColor },
    "transparency" to FloatGetter<WaterMaterial> { it.transparency },
    "waveScale" to FloatGetter<WaterMaterial> { it.waveScale },
    "waveMagnitude" to FloatGetter<WaterMaterial> { it.waveMagnitude },
), CompositeSupplier {

    override val child
        get() = sky
}

internal class FogMaterial(
    val density: Float,
    val fogColor: ColorRGB,
) : InternalPostProcessingMaterial(
    "!shader/effect/fog.frag",
    "fogColor" to ColorRGBGetter<FogMaterial> { it.fogColor },
    "density" to FloatGetter<FogMaterial> { it.density },
)

internal class InternalMultiPassEffect(
    val effectPasses: List<InternalPassDeclaration>,
    val keepTextures: Set<String>,
    val finalMaterialModifier: InternalMaterialModifier,
    override val nodeContext: NodeContext
) : NodeKeeper