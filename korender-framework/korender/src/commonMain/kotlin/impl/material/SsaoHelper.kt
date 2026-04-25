package com.zakgof.korender.impl.material

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
