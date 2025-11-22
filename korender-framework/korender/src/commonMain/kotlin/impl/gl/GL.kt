package com.zakgof.korender.impl.gl

import com.zakgof.korender.impl.buffer.NativeBuffer
import com.zakgof.korender.impl.buffer.NativeByteBuffer

expect object GL {

    val shaderEnv: String

    fun glDrawElements(mode: GLConstant, count: Int, type: GLConstant, indices: Int)

    fun glDrawElementsInstanced(mode: GLConstant, count: Int, type: GLConstant, indices: Int, instances: Int)

    fun glDrawArrays(mode: GLConstant, starting: Int, count: Int)

    fun glDrawArraysInstanced(mode: GLConstant, starting: Int, count: Int, instances: Int)

    fun glEnable(target: GLConstant)

    // fun glPolygonMode(sides: Int, mode: Int)

    fun glDisable(target: GLConstant)

    fun glBindTexture(target: GLConstant, texture: GLTexture?)

    fun glTexParameterf(target: GLConstant, pname: Int, param: Float)

    fun glDeleteTextures(texture: GLTexture)

    fun glPixelStorei(pname: GLConstant, param: Int)

    fun glGenTextures(): GLTexture

    fun glBlendFunc(sfactor: GLConstant, dfactor: GLConstant)

    fun glDepthFunc(func: GLConstant)

    fun glDepthMask(flag: Boolean)

    fun glCullFace(mode: GLConstant)

    fun glTexImage2D(target: GLConstant, level: Int, internalformat: GLConstant, width: Int, height: Int, border: Int, format: GLConstant, type: GLConstant, buffer: NativeBuffer?)

    fun glTexImage3D(target: GLConstant, level: Int, internalformat: GLConstant, width: Int, height: Int, depth: Int, border: Int, format: GLConstant, type: GLConstant, buffer: NativeBuffer?)

    fun glTexSubImage2D(target: GLConstant, level: Int, x: Int, y: Int, width: Int, height: Int, format: GLConstant, type: GLConstant, buffer: NativeBuffer)

    fun glTexSubImage3D(target: GLConstant, level: Int, x: Int, y: Int, z: Int, width: Int, height: Int, depth: Int, format: GLConstant, type: GLConstant, buffer: NativeBuffer)

    fun glGetTexImage(tex: GLConstant, level: Int, format: GLConstant, type: GLConstant, pixels: NativeByteBuffer)

    fun glGetFloatv(pname: GLConstant): Float?

    fun glGetError(): Int

    fun glClear(mask: GLBitConstant)

    fun glViewport(x: Int, y: Int, w: Int, h: Int)

    fun glTexParameteri(target: GLConstant, pname: GLConstant, param: GLConstant)

    fun glTexParameterfv(target: GLConstant, pname: GLConstant, param: FloatArray)

    fun glClearColor(fl: Float, fl1: Float, fl2: Float, fl3: Float)

    fun glClearDepth(fl: Float)

    fun glActiveTexture(texture: GLConstant)

    fun glBindBuffer(target: GLConstant, buffer: GLBuffer)

    fun glBindVertexArray(vertexArray: GLVertexArray?)

    fun glBindAttribLocation(program: GLProgram, index: Int, attr: String)

    fun glBufferData(target: GLConstant, data: NativeByteBuffer, usage: Int)

    fun glBufferData(target: GLConstant, size: Long, usage: Int)

    fun glGenBuffers(): GLBuffer

    fun glGenVertexArrays(): GLVertexArray

    fun glDeleteBuffers(buffer: GLBuffer)

    fun glDeleteVertexArrays(vertexArray: GLVertexArray)

    fun glCreateProgram(): GLProgram

    fun glCreateShader(type: GLConstant): GLShader

    fun glAttachShader(program: GLProgram, shader: GLShader)

    fun glLinkProgram(program: GLProgram)

    fun glUseProgram(program: GLProgram?)

    fun glValidateProgram(program: GLProgram)

    fun glGetProgrami(program: GLProgram, pname: GLConstant): Int

    fun glGetShaderInfoLog(shader: GLShader): String

    fun glGetProgramInfoLog(program: GLProgram): String

    fun glGetActiveUniform(program: GLProgram, index: Int): String

    fun glGetActiveAttrib(program: GLProgram, index: Int): String

    fun glShaderSource(shader: GLShader, source: String)

    fun glCompileShader(shader: GLShader)

    fun glEnableVertexAttribArray(index: Int)

    fun glVertexAttribDivisor(index: Int, divisor: Int)

    fun glGetUniformLocation(program: GLProgram, name: String): GLUniformLocation?

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
        type: GLConstant,
        normalized: Boolean,
        stride: Int,
        pointer: Int
    )

    fun glVertexAttribIPointer(
        index: Int,
        size: Int,
        type: GLConstant,
        stride: Int,
        pointer: Int
    )

    fun glGetShaderi(shader: GLShader, pname: Int): Int

    fun glDeleteShader(shader: GLShader)

    fun glDeleteProgram(program: GLProgram)

    fun glGenerateMipmap(target: GLConstant)

    fun glGenFramebuffers(): GLFrameBuffer

    fun glFramebufferTexture2D(
        target: GLConstant,
        attachment: GLConstant,
        textarget: GLConstant,
        texture: GLTexture,
        level: Int
    )

    fun glDeleteFramebuffers(framebuffer: GLFrameBuffer)

    fun glBindFramebuffer(target: GLConstant, framebuffer: GLFrameBuffer?)

    fun glCheckFramebufferStatus(target: GLConstant): Int

    fun glDrawBuffers(vararg targets: GLConstant)

    fun glReadPixels(x: Int, y: Int, width: Int, height: Int, format: GLConstant, type: GLConstant, data: NativeByteBuffer)

    fun glGetMaxTextureMaxAnisotropyConstant(): Int

    fun glGetTextureMaxAnisotropyConstant(): Int

    fun glGetUniformBlockIndex(program: GLProgram, name: String): Int

    fun glGetActiveUniformBlockiv(program: GLProgram, blockIndex: Int, param: Int, paramValues: IntArray)

    fun glGetActiveUniformsiv(program: GLProgram, uniformIndices: IntArray, param: Int, paramValues: IntArray)

    fun glGetActiveUniformName(program: GLProgram, uniformIndex: Int): String

    fun glUniformBlockBinding(program: GLProgram, blockIndex: Int, blockBinding: Int)

    fun glBindBufferBase(target: GLConstant, blockBinding: Int, buffer: GLBuffer)

    fun glBufferSubData(target: GLConstant, offset: Long, buffer: NativeByteBuffer)

    fun glBindBufferRange(target: GLConstant, blockBinding: Int, buffer: GLBuffer, shift: Int, size: Int)

    fun glGetInteger(pname: GLConstant): Int

}