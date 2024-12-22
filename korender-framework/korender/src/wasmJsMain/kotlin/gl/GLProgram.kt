package com.zakgof.korender.gl

import org.khronos.webgl.WebGLProgram

actual class GLProgram(internal val program: WebGLProgram) {
    override fun toString() = program.toString()
}