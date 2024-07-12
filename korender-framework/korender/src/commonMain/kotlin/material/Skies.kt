package com.zakgof.korender.material

import com.zakgof.korender.uniforms.FastCloudSkyUniforms
import com.zakgof.korender.uniforms.UniformSupplier

object Skies {
    val FastCloud = Sky("sky/fastcloud.plugin.frag") { FastCloudSkyUniforms() }
    val Cloud = Sky("sky/cloud.plugin.frag") { UniformSupplier { null } }
    val Star = Sky("sky/star.plugin.frag") { UniformSupplier { null } }
}