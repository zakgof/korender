package com.zakgof.korender.gl

actual class GLVertexArray(internal val glHandle: Int) {
    override fun toString() = glHandle.toString()
}