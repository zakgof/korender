package com.zakgof.korender.impl.gl

actual class GLUniformLocation(val glHandle: Int) {
    override fun toString() = glHandle.toString()
}