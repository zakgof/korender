package com.zakgof.korender.impl.gl

actual class GLFrameBuffer(internal val glHandle: Int) {
    override fun toString() = glHandle.toString()
}