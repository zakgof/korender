package com.zakgof.korender.impl.gl

actual class GLVertexArray(internal val glHandle: Int) {
    override fun toString() = glHandle.toString()
}