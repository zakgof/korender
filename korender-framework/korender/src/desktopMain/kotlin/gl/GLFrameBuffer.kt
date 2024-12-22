package com.zakgof.korender.gl

actual class GLFrameBuffer(internal val glHandle: Int) {
    override fun toString() = glHandle.toString()
}