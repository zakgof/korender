package com.zakgof.korender.gl

import java.nio.FloatBuffer
import java.nio.IntBuffer

interface IGL20 {
    fun glCreateProgram(): Int

    fun glCreateShader(type: Int): Int

    fun glAttachShader(program: Int, shader: Int)

    fun glLinkProgram(program: Int)

    fun glUseProgram(program: Int)

    fun glValidateProgram(program: Int)

    fun glGetProgrami(program: Int, pname: Int): Int

    fun glGetShaderInfoLog(shader: Int): String

    fun glGetProgramInfoLog(program: Int): String

    fun glGetProgramiv(program: Int, pname: Int, params: IntBuffer)

    fun glGetActiveUniform(program: Int, index: Int, size: IntBuffer, type: IntBuffer): String

    fun glGetActiveAttrib(program: Int, index: Int, size: IntBuffer, type: IntBuffer): String

    fun glShaderSource(shader: Int, source: String)

    fun glCompileShader(shader: Int)

    fun glGetShaderiv(shader: Int, pname: Int, params: IntBuffer)

    fun glEnableVertexAttribArray(index: Int)

    fun glGetUniformLocation(program: Int, name: String): Int

    fun glGetAttribLocation(program: Int, name: String): Int

    fun glUniform1i(location: Int, v0: Int)

    fun glUniform3f(location: Int, v0: Float, v1: Float, v2: Float)

    fun glUniformMatrix2fv(location: Int, transpose: Boolean, value: FloatBuffer)

    fun glUniformMatrix3fv(location: Int, transpose: Boolean, value: FloatBuffer)

    fun glUniformMatrix4fv(location: Int, transpose: Boolean, value: FloatBuffer)

    fun glVertexAttribPointer(
        index: Int,
        size: Int,
        type: Int,
        normalized: Boolean,
        stride: Int,
        pointer: Int
    )

    fun glGetShaderi(shader: Int, pname: Int): Int

    fun glUniform1f(location: Int, v0: Float)
}
