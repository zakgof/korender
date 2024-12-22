package com.zakgof.korender.gl

import org.khronos.webgl.WebGLBuffer

actual class GLBuffer(internal val buffer: WebGLBuffer) {
    override fun toString() = buffer.toString()
}