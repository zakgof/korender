package com.zakgof.korender.impl.gl

actual class GLShader(internal val glHandle: Int) {
    override fun toString() = glHandle.toString()
}