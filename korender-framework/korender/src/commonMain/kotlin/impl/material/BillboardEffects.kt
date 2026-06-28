package com.zakgof.korender.impl.material

import com.zakgof.korender.impl.glgpu.FloatGetter

internal class FireEffect(
    val strength: Float,
) : InternalBillboardEffect(
    "!shader/effect/fire.frag",
    "strength" to FloatGetter<FireEffect> { it.strength }
)

internal class FireballEffect(
    val power: Float,
) : InternalBillboardEffect(
    "!shader/effect/fireball.frag",
    "power" to FloatGetter<FireballEffect> { it.power }
)

internal class SmokeEffect(
    val density: Float,
    val seed: Float,
) : InternalBillboardEffect(
    "!shader/effect/smoke.frag",
    "density" to FloatGetter<SmokeEffect> { it.density },
    "seed" to FloatGetter<SmokeEffect> { it.seed }
)