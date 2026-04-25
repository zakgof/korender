package com.zakgof.korender.impl.material

import com.zakgof.korender.impl.glgpu.FloatGetter
import com.zakgof.korender.impl.glgpu.Vec2Getter
import com.zakgof.korender.math.Vec2

internal class SsaoMaterial :
    InternalPostProcessingMaterial("!shader/effect/ssao.frag")

internal class SsaoBlurMaterial(
    val direction: Vec2,
    val radius: Float,
) : InternalPostProcessingMaterial(
    "!shader/effect/ssao_blur.frag",
    "direction" to Vec2Getter<SsaoBlurMaterial> { it.direction },
    "radius" to FloatGetter<SsaoBlurMaterial> { it.radius },
)
