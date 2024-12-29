package com.zakgof.korender.impl.gl

actual class GLProgram(internal val glHandle: Int) {
    override fun toString() = glHandle.toString()
}