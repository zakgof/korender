package com.zakgof.korender.gl

actual class GLTexture(internal val glHandle: Int) {
    override fun toString() = glHandle.toString()
}