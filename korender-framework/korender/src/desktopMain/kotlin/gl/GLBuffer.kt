package com.zakgof.korender.gl

actual class GLBuffer(internal val glHandle: Int) {
    override fun toString() = glHandle.toString()
}