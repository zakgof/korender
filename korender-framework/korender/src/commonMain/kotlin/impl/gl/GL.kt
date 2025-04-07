package com.zakgof.korender.impl.gl

import com.zakgof.korender.impl.buffer.NativeByteBuffer

expect object GL {

    val shaderEnv: String

    fun glDrawElements(mode: Int, count: Int, type: Int, indices: Int)

    fun glDrawArrays(mode: Int, starting: Int, count: Int)

    fun glEnable(target: Int)

    // fun glPolygonMode(sides: Int, mode: Int)

    fun glDisable(target: Int)

    fun glBindTexture(target: Int, texture: GLTexture?)

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
        pixels: NativeByteBuffer?
    )

    fun glGetFloatv(pname: Int): Float?

    fun glGetError(): Int

    fun glClear(mask: Int)

    fun glViewport(x: Int, y: Int, w: Int, h: Int)

    fun glTexParameteri(target: Int, pname: Int, param: Int)

    fun glTexParameterfv(target: Int, pname: Int, param: FloatArray)

    fun glClearColor(fl: Float, fl1: Float, fl2: Float, fl3: Float)

    fun glClearDepth(fl: Float)

    fun glActiveTexture(texture: Int)

    fun glBindBuffer(target: Int, buffer: GLBuffer)

    fun glBindVertexArray(vertexArray: GLVertexArray?)

    fun glBindAttribLocation(program: GLProgram, index: Int, attr: String)

    fun glBufferData(target: Int, data: NativeByteBuffer, usage: Int)

    fun glGenBuffers(): GLBuffer

    fun glGenVertexArrays(): GLVertexArray

    fun glDeleteBuffers(buffer: GLBuffer)

    fun glDeleteVertexArrays(vertexArray: GLVertexArray)

    fun glCreateProgram(): GLProgram

    fun glCreateShader(type: Int): GLShader

    fun glAttachShader(program: GLProgram, shader: GLShader)

    fun glLinkProgram(program: GLProgram)

    fun glUseProgram(program: GLProgram?)

    fun glValidateProgram(program: GLProgram)

    fun glGetProgrami(program: GLProgram, pname: Int): Int

    fun glGetShaderInfoLog(shader: GLShader): String

    fun glGetProgramInfoLog(program: GLProgram): String

    fun glGetActiveUniform(program: GLProgram, index: Int): String

    fun glGetActiveAttrib(program: GLProgram, index: Int): String

    fun glShaderSource(shader: GLShader, source: String)

    fun glCompileShader(shader: GLShader)

    fun glEnableVertexAttribArray(index: Int)

    fun glGetUniformLocation(program: GLProgram, name: String): GLUniformLocation

    fun glGetAttribLocation(program: GLProgram, name: String): Int

    fun glUniform1i(location: GLUniformLocation, v0: Int)
    fun glUniform1iv(location: GLUniformLocation, vararg v0: Int)
    fun glUniform1f(location: GLUniformLocation, v0: Float)
    fun glUniform2f(location: GLUniformLocation, v0: Float, v1: Float)
    fun glUniform3f(location: GLUniformLocation, v0: Float, v1: Float, v2: Float)
    fun glUniform4f(location: GLUniformLocation, v0: Float, v1: Float, v2: Float, v3: Float)
    fun glUniform1fv(location: GLUniformLocation, value: FloatArray)
    fun glUniform3fv(location: GLUniformLocation, value: FloatArray)
    fun glUniform4fv(location: GLUniformLocation, value: FloatArray)
    fun glUniformMatrix2fv(location: GLUniformLocation, transpose: Boolean, value: FloatArray)
    fun glUniformMatrix3fv(location: GLUniformLocation, transpose: Boolean, value: FloatArray)
    fun glUniformMatrix4fv(location: GLUniformLocation, transpose: Boolean, value: FloatArray)

    fun glVertexAttribPointer(
        index: Int,
        size: Int,
        type: Int,
        normalized: Boolean,
        stride: Int,
        pointer: Int
    )

    fun glVertexAttribIPointer(
        index: Int,
        size: Int,
        type: Int,
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

    fun glFramebufferTexture(target: Int, attachment: Int, texture: GLTexture, level: Int)

    fun glDeleteFramebuffers(framebuffer: GLFrameBuffer)

    fun glBindFramebuffer(target: Int, framebuffer: GLFrameBuffer?)

    fun glCheckFramebufferStatus(target: Int): Int

    fun glDrawBuffers(vararg targets: Int)
}