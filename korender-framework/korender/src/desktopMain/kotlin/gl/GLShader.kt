package com.zakgof.korender.gl

actual class GLShader(internal val glHandle: Int) {
    override fun toString() = glHandle.toString()
}