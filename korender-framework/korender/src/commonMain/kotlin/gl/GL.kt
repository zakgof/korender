package com.zakgof.korender.gl

import com.zakgof.korender.buffer.BufferData
import com.zakgof.korender.buffer.Byter
import com.zakgof.korender.buffer.Floater
import com.zakgof.korender.buffer.Inter

expect object GL {

    val shaderEnv: String

    fun glDrawElements(mode: Int, count: Int, type: Int, indices: Int)

    fun glEnable(target: Int)

    fun glDisable(target: Int)

    fun glBindTexture(target: Int, texture: GLTexture)

    fun glTexParameterf(target: Int, pname: Int, param: Float)

    fun glDeleteTextures(texture: GLTexture)

    fun glPixelStorei(pname: Int, param: Int)

    fun glGenTextures(): GLTexture

    fun glBlendFunc(sfactor: Int, dfactor: Int)

    fun glDepthFunc(func: Int)

    fun glDepthMask(flag: Boolean)

    fun glCullFace(mode: Int)

    fun glTexImage2D(
        target: Int,
        level: Int,
        internalformat: Int,
        width: Int,
        height: Int,
        border: Int,
        format: Int,
        type: Int,
        pixels: Byter?
    )

    fun glGetFloatv(pname: Int) : Float?

    fun glGetError(): Int

    fun glClear(mask: Int)

    fun glViewport(x: Int, y: Int, w: Int, h: Int)

    fun glTexParameteri(target: Int, pname: Int, param: Int)

    fun glTexParameterfv(target: Int, pname: Int, param: FloatArray)

    fun glClearColor(fl: Float, fl1: Float, fl2: Float, fl3: Float)

    fun glActiveTexture(texture: Int)

    fun glBindBuffer(target: Int, buffer: GLBuffer)

    fun glBufferData(target: Int, data: BufferData<out Any>, usage: Int)

    fun glGenBuffers(): GLBuffer

    fun glDeleteBuffers(buffer: GLBuffer)

    fun glCreateProgram(): GLProgram

    fun glCreateShader(type: Int): GLShader

    fun glAttachShader(program: GLProgram, shader: GLShader)

    fun glLinkProgram(program: GLProgram)

    fun glUseProgram(program: GLProgram?)

    fun glValidateProgram(program: GLProgram)

    fun glGetProgrami(program: GLProgram, pname: Int): Int

    fun glGetShaderInfoLog(shader: GLShader): String

    fun glGetProgramInfoLog(program: GLProgram): String

    fun glGetProgramiv(program: GLProgram, pname: Int, params: Inter)

    fun glGetActiveUniform(program: GLProgram, index: Int, size: Inter, type: Inter): String

    fun glGetActiveAttrib(program: GLProgram, index: Int, size: Inter, type: Inter): String

    fun glShaderSource(shader: GLShader, source: String)

    fun glCompileShader(shader: GLShader)

    fun glGetShaderiv(shader: GLShader, pname: Int, params: Inter)

    fun glEnableVertexAttribArray(index: Int)

    fun glGetUniformLocation(program: GLProgram, name: String): GLUniformLocation

    fun glGetAttribLocation(program: GLProgram, name: String): Int

    fun glUniform1i(location: GLUniformLocation, v0: Int)
    fun glUniform1f(location: GLUniformLocation, v0: Float)
    fun glUniform2f(location: GLUniformLocation, v0: Float, v1: Float)
    fun glUniform3f(location: GLUniformLocation, v0: Float, v1: Float, v2: Float)
    fun glUniform4f(location: GLUniformLocation, v0: Float, v1: Float, v2: Float, v3: Float)
    fun glUniformMatrix2fv(location: GLUniformLocation, transpose: Boolean, value: Floater)
    fun glUniformMatrix3fv(location: GLUniformLocation, transpose: Boolean, value: Floater)
    fun glUniformMatrix4fv(location: GLUniformLocation, transpose: Boolean, value: Floater)

    fun glVertexAttribPointer(
        index: Int,
        size: Int,
        type: Int,
        normalized: Boolean,
        stride: Int,
        pointer: Int
    )

    fun glGetShaderi(shader: GLShader, pname: Int): Int

    fun glDeleteShader(shader: GLShader)

    fun glDeleteProgram(program: GLProgram)

    fun glGenerateMipmap(target: Int)

    fun glGenFramebuffers(): GLFrameBuffer

    fun glFramebufferTexture2D(
        target: Int,
        attachment: Int,
        textarget: Int,
        texture: GLTexture,
        level: Int
    )

    fun glDeleteFramebuffers(framebuffer: GLFrameBuffer)

    fun glBindFramebuffer(target: Int, framebuffer: GLFrameBuffer?)

    fun glCheckFramebufferStatus(target: Int): Int
}