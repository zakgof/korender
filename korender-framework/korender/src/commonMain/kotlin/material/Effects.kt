package com.zakgof.korender.material

import com.zakgof.korender.uniforms.AdjustUniforms
import com.zakgof.korender.uniforms.FireBallUniforms
import com.zakgof.korender.uniforms.FireUniforms
import com.zakgof.korender.uniforms.SmokeUniforms
import com.zakgof.korender.uniforms.WaterUniforms

object Effects {
    val Fire = Effect("effect/fire.frag") { FireUniforms() }
    val FireBall = Effect("effect/fireball.frag") { FireBallUniforms() }
    val Smoke = Effect("effect/smoke.frag") { SmokeUniforms() }
    val Water = Effect("effect/water.frag") { WaterUniforms() }
    val Adjust = Effect("effect/adjust.frag") { AdjustUniforms() }
}