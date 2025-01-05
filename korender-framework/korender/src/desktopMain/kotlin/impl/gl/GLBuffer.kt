package com.zakgof.korender.impl.gl

actual class GLBuffer(internal val glHandle: Int) {
    override fun toString() = glHandle.toString()
}