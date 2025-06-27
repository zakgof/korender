package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.impl.gl.GLTexture

internal interface GLBindableTexture {
    val glHandle: GLTexture
    fun bind(unit: Int)
}