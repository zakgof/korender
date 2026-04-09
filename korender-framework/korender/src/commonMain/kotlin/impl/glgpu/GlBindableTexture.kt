package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.impl.gl.GLTexture

internal interface GlBindableTexture : AutoCloseable {
    val glHandle: GLTexture
    val unit: Int
    fun bind(unit: Int)
}
