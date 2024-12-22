package com.zakgof.korender.gl

actual class GLUniformLocation(val glHandle: Int) {
    override fun toString() = glHandle.toString()
}