package com.zakgof.korender.impl.material

import com.zakgof.korender.impl.glgpu.FloatGetter

internal class FireEffect(
    val strength: Float,
) : InternalBillboardEffect(
    "!shader/effect/fire.frag",
    "strength" to FloatGetter<FireEffect> { it.strength }
)