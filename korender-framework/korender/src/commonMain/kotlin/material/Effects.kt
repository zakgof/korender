package com.zakgof.korender.material

import com.zakgof.korender.uniforms.AdjustUniforms
import com.zakgof.korender.uniforms.BlurUniforms
import com.zakgof.korender.uniforms.FireBallUniforms
import com.zakgof.korender.uniforms.FireUniforms
import com.zakgof.korender.uniforms.SmokeUniforms
import com.zakgof.korender.uniforms.UniformSupplier
import com.zakgof.korender.uniforms.WaterUniforms

object Effects {
    val Identity = Effect("shader/effect/identity.frag") { UniformSupplier { null } }
    val Fire = Effect("shader/effect/fire.frag") { FireUniforms() }
    val FireBall = Effect("shader/effect/fireball.frag") { FireBallUniforms() }
    val Smoke = Effect("shader/effect/smoke.frag") { SmokeUniforms() }
    val Water = Effect("shader/effect/water.frag") { WaterUniforms() }
    val Adjust = Effect("shader/effect/adjust.frag") { AdjustUniforms() }
    val Fxaa = Effect("shader/effect/fxaa.frag") { UniformSupplier { null } }
    val BlurHorz = Effect("shader/effect/blurh.frag") { BlurUniforms() }
    val BlurVert = Effect("shader/effect/blurv.frag") { BlurUniforms() }
}