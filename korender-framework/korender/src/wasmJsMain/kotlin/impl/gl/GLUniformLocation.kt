package com.zakgof.korender.impl.gl

import org.khronos.webgl.WebGLUniformLocation

actual class GLUniformLocation(val uniformLocation: WebGLUniformLocation) {
    override fun toString() = uniformLocation.toString()
}

