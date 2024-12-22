package com.zakgof.korender.material

import com.zakgof.korender.uniforms.FastCloudSkyUniforms
import com.zakgof.korender.uniforms.UniformSupplier

object Skies {
    val FastCloud = Sky("shader/sky/fastcloud.plugin.frag") { FastCloudSkyUniforms() }
    val Cloud = Sky("shader/sky/cloud.plugin.frag") { UniformSupplier { null } }
    val Star = Sky("shader/sky/star.plugin.frag") { UniformSupplier { null } }
}