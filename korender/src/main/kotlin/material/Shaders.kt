package com.zakgof.korender.material

import com.zakgof.korender.Gpu
import com.zakgof.korender.gpu.GpuShader

object Shaders {
    fun standard(gpu: Gpu, vararg defs: String): GpuShader = ShaderBuilder("test.vert", "test.frag", *defs).build(gpu)
}
