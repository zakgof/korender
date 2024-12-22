package com.zakgof.korender.gl

actual class GLProgram(internal val glHandle: Int) {
    override fun toString() = glHandle.toString()
}