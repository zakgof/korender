package com.zakgof.korender.lwjgl

import org.lwjgl.opengl.GL20
import java.nio.FloatBuffer
import java.nio.IntBuffer

class Lwjgl20 : com.zakgof.korender.impl.gl.IGL20 {
    override fun glCreateProgram(): Int {
        return GL20.glCreateProgram()
    }

    override fun glCreateShader(type: Int): Int {
        return GL20.glCreateShader(type)
    }

    override fun glAttachShader(program: Int, shader: Int) {
        GL20.glAttachShader(program, shader)
    }

    override fun glLinkProgram(program: Int) {
        GL20.glLinkProgram(program)
    }

    override fun glUseProgram(program: Int) {
        GL20.glUseProgram(program)
    }

    override fun glValidateProgram(program: Int) {
        GL20.glValidateProgram(program)
    }

    override fun glGetProgrami(program: Int, pname: Int): Int {
        return GL20.glGetProgrami(program, pname)
    }

    override fun glGetShaderInfoLog(shader: Int): String {
        return GL20.glGetShaderInfoLog(shader)
    }

    override fun glGetProgramInfoLog(program: Int): String {
        return GL20.glGetProgramInfoLog(program)
    }

    override fun glGetProgramiv(program: Int, pname: Int, params: IntBuffer) {
        GL20.glGetProgramiv(program, pname, params)
    }

    override fun glGetActiveUniform(program: Int, index: Int, size: IntBuffer, type: IntBuffer): String {
        return GL20.glGetActiveUniform(program, index, size, type)
    }

    override fun glGetActiveAttrib(program: Int, index: Int, size: IntBuffer, type: IntBuffer): String {
        return GL20.glGetActiveAttrib(program, index, size, type)
    }

    override fun glShaderSource(shader: Int, source: String) {
        GL20.glShaderSource(shader, source)
    }

    override fun glCompileShader(shader: Int) {
        GL20.glCompileShader(shader)
    }

    override fun glGetShaderiv(shader: Int, pname: Int, params: IntBuffer) {
        GL20.glGetShaderiv(shader, pname, params)
    }

    override fun glEnableVertexAttribArray(index: Int) {
        GL20.glEnableVertexAttribArray(index)
    }

    override fun glGetUniformLocation(program: Int, name: String): Int {
        return GL20.glGetUniformLocation(program, name)
    }

    override fun glGetAttribLocation(program: Int, name: String): Int {
        return GL20.glGetAttribLocation(program, name)
    }

    override fun glUniform1i(location: Int, v0: Int) {
        GL20.glUniform1i(location, v0)
    }

    override fun glUniform2f(location: Int, v0: Float, v1: Float) {
        GL20.glUniform2f(location, v0, v1)
    }

    override fun glUniform3f(location: Int, v0: Float, v1: Float, v2: Float) {
        GL20.glUniform3f(location, v0, v1, v2)
    }

    override fun glUniform4f(location: Int, v0: Float, v1: Float, v2: Float, v3: Float) =
        GL20.glUniform4f(location, v0, v1, v2, v3)


    override fun glUniformMatrix2fv(location: Int, transpose: Boolean, value: FloatBuffer) {
        GL20.glUniformMatrix2fv(location, transpose, value)
    }

    override fun glUniformMatrix3fv(location: Int, transpose: Boolean, value: FloatBuffer) {
        GL20.glUniformMatrix3fv(location, transpose, value)
    }

    override fun glUniformMatrix4fv(location: Int, transpose: Boolean, value: FloatBuffer) {
        GL20.glUniformMatrix4fv(location, transpose, value)
    }

    override fun glVertexAttribPointer(
        index: Int,
        size: Int,
        type: Int,
        normalized: Boolean,
        stride: Int,
        pointer: Int
    ) {
        GL20.glVertexAttribPointer(index, size, type, normalized, stride, pointer.toLong())
    }

    override fun glGetShaderi(shader: Int, pname: Int): Int {
        return GL20.glGetShaderi(shader, pname)
    }

    override fun glUniform1f(location: Int, v0: Float) {
        GL20.glUniform1f(location, v0)
    }

    override fun glDeleteShader(shader: Int) = GL20.glDeleteShader(shader)

    override fun glDeleteProgram(program: Int) = GL20.glDeleteProgram(program)
}
