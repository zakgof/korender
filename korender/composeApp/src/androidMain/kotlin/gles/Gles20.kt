package com.zakgof.korender.gles

import android.opengl.GLES20
import com.zakgof.korender.gl.IGL20
import java.nio.FloatBuffer
import java.nio.IntBuffer

object Gles20 : IGL20 {
    override fun glCreateProgram(): Int = GLES20.glCreateProgram()

    override fun glCreateShader(type: Int) = GLES20.glCreateShader(type)

    override fun glAttachShader(program: Int, shader: Int) = GLES20.glAttachShader(program, shader)

    override fun glLinkProgram(program: Int) = GLES20.glLinkProgram(program)

    override fun glUseProgram(program: Int) = GLES20.glUseProgram(program)

    override fun glValidateProgram(program: Int) = GLES20.glValidateProgram(program)

    override fun glGetProgrami(program: Int, pname: Int): Int {
        val params = IntArray(1)
        GLES20.glGetProgramiv(program, pname, params, 0)
        return params[0]
    }

    override fun glGetShaderInfoLog(shader: Int) = GLES20.glGetShaderInfoLog(shader)

    override fun glGetProgramInfoLog(program: Int): String = GLES20.glGetProgramInfoLog(program)

    override fun glGetProgramiv(program: Int, pname: Int, params: IntBuffer) = GLES20.glGetProgramiv(program, pname, params)

    override fun glGetActiveUniform(
        program: Int,
        index: Int,
        size: IntBuffer,
        type: IntBuffer
    ): String = GLES20.glGetActiveUniform(program, index, size, type)

    override fun glGetActiveAttrib(
        program: Int,
        index: Int,
        size: IntBuffer,
        type: IntBuffer
    ): String {
        return GLES20.glGetActiveAttrib(program, index, size, type)
    }

    override fun glShaderSource(shader: Int, source: String) {
        GLES20.glShaderSource(shader, source)
    }

    override fun glCompileShader(shader: Int) {
        GLES20.glCompileShader(shader)
    }

    override fun glGetShaderiv(shader: Int, pname: Int, params: IntBuffer) {
        GLES20.glGetShaderiv(shader, pname, params)
    }

    override fun glEnableVertexAttribArray(index: Int) {
        GLES20.glEnableVertexAttribArray(index)
    }

    override fun glGetUniformLocation(program: Int, name: String): Int {
        return GLES20.glGetUniformLocation(program, name)
    }

    override fun glGetAttribLocation(program: Int, name: String): Int {
        return GLES20.glGetAttribLocation(program, name)
    }

    override fun glUniform1i(location: Int, v0: Int) {
        GLES20.glUniform1i(location, v0)
    }

    override fun glUniform3f(location: Int, v0: Float, v1: Float, v2: Float) {
        GLES20.glUniform3f(location, v0, v1, v2)
    }

    override fun glUniformMatrix2fv(location: Int, transpose: Boolean, value: FloatBuffer) {
        GLES20.glUniformMatrix2fv(location, 1, transpose, value)
    }

    override fun glUniformMatrix3fv(location: Int, transpose: Boolean, value: FloatBuffer) {
        GLES20.glUniformMatrix3fv(location, 1, transpose, value)
    }

    override fun glUniformMatrix4fv(location: Int, transpose: Boolean, value: FloatBuffer) {
        GLES20.glUniformMatrix4fv(location, 1, transpose, value)
    }

    override fun glVertexAttribPointer(
        index: Int,
        size: Int,
        type: Int,
        normalized: Boolean,
        stride: Int,
        pointer: Int
    ) {
        GLES20.glVertexAttribPointer(index, size, type, normalized, stride, pointer)
    }

    override fun glGetShaderi(shader: Int, pname: Int): Int {
        val params = IntArray(1)
        GLES20.glGetShaderiv(shader, pname, params, 0)
        return params[0]
    }

    override fun glUniform1f(location: Int, v0: Float) {
        GLES20.glUniform1f(location, v0)
    }
}
