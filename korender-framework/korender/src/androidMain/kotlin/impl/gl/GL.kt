package com.zakgof.korender.impl.gl

import android.opengl.GLES11
import android.opengl.GLES20
import android.opengl.GLES30
import com.zakgof.korender.impl.buffer.NativeByteBuffer

actual object GL {

    actual val shaderEnv = "GLES"

    actual fun glDrawElements(mode: Int, count: Int, type: Int, indices: Int) =
        GLES11.glDrawElements(mode, count, type, indices)

    actual fun glDrawArrays(mode: Int, starting: Int, count: Int) =
        GLES11.glDrawArrays(mode, starting, count)

    actual fun glEnable(target: Int) = GLES11.glEnable(target)

    actual fun glDisable(target: Int) = GLES11.glDisable(target)

    actual fun glBindTexture(target: Int, texture: GLTexture?) =
        GLES11.glBindTexture(target, texture?.texture ?: 0)

    actual fun glTexParameterf(target: Int, pname: Int, param: Float) =
        GLES11.glTexParameterf(target, pname, param)

    actual fun glDeleteTextures(texture: GLTexture) =
        GLES11.glDeleteTextures(1, intArrayOf(texture.texture), 0)

    actual fun glPixelStorei(pname: Int, param: Int) = GLES11.glPixelStorei(pname, param)
    actual fun glGenTextures(): GLTexture = GLTexture(IntArray(1).also {
            GLES11.glGenTextures(1, it, 0)
        }[0])

    actual fun glBlendFunc(sfactor: Int, dfactor: Int) = GLES11.glBlendFunc(sfactor, dfactor)

    actual fun glDepthFunc(func: Int) = GLES11.glDepthFunc(func)
    actual fun glDepthMask(flag: Boolean) = GLES11.glDepthMask(flag)
    actual fun glCullFace(mode: Int) = GLES11.glCullFace(mode)
    actual fun glTexImage2D(
        target: Int,
        level: Int,
        internalformat: Int,
        width: Int,
        height: Int,
        border: Int,
        format: Int,
        type: Int,
        pixels: NativeByteBuffer?
    ) = GLES11.glTexImage2D(
        target, level, internalformat, width, height, border, format, type, pixels?.byteBuffer
    )

    actual fun glGetFloatv(pname: Int): Float? {
        val fa = FloatArray(1)
        GLES11.glGetFloatv(pname, fa, 0)
        return fa[0]
    }

    actual fun glGetError(): Int = GLES11.glGetError()

    actual fun glClear(mask: Int) = GLES11.glClear(mask)

    actual fun glClearColor(fl: Float, fl1: Float, fl2: Float, fl3: Float) =
        GLES11.glClearColor(fl, fl1, fl2, fl3)

    actual fun glViewport(x: Int, y: Int, w: Int, h: Int) = GLES11.glViewport(x, y, w, h)

    actual fun glTexParameteri(target: Int, pname: Int, param: Int) =
        GLES11.glTexParameteri(target, pname, param)

    actual fun glTexParameterfv(target: Int, pname: Int, param: FloatArray) =
        GLES11.glTexParameterfv(target, pname, param, 0)

    actual fun glActiveTexture(texture: Int) = GLES20.glActiveTexture(texture)

    actual fun glBindBuffer(target: Int, buffer: GLBuffer) =
        GLES20.glBindBuffer(target, buffer.buffer)

    actual fun glBufferData(target: Int, data: NativeByteBuffer, usage: Int) =
        GLES20.glBufferData(target, data.byteBuffer.remaining(), data.byteBuffer, usage)

    actual fun glGenBuffers() =
        com.zakgof.korender.impl.gl.GLBuffer(IntArray(1).also { GLES20.glGenBuffers(1, it, 0) }[0])

    actual fun glDeleteBuffers(buffer: GLBuffer) =
        GLES20.glDeleteBuffers(1, intArrayOf(buffer.buffer), 0)

    actual fun glCreateProgram() = com.zakgof.korender.impl.gl.GLProgram(GLES20.glCreateProgram())

    actual fun glCreateShader(type: Int) =
        com.zakgof.korender.impl.gl.GLShader(GLES20.glCreateShader(type))

    actual fun glAttachShader(program: GLProgram, shader: GLShader) =
        GLES20.glAttachShader(program.program, shader.glHandle)

    actual fun glLinkProgram(
        program: GLProgram
    ) = GLES20.glLinkProgram(program.program)

    actual fun glUseProgram(
        program: GLProgram?
    ) = GLES20.glUseProgram(
        program?.program ?: 0
    )

    actual fun glValidateProgram(program: GLProgram) =
        GLES20.glValidateProgram(program.program)

    actual fun glGetProgrami(program: GLProgram, pname: Int): Int =
        intViaArray { GLES20.glGetProgramiv(program.program, pname, it, 0) }

    actual fun glGetShaderInfoLog(shader: GLShader): String =
        GLES20.glGetShaderInfoLog(shader.glHandle)

    actual fun glGetProgramInfoLog(program: GLProgram): String =
        GLES20.glGetProgramInfoLog(program.program)

    actual fun glGetActiveUniform(program: GLProgram, index: Int): String =
        GLES20.glGetActiveUniform(program.program, index, IntArray(1) { 0 }, 0, IntArray(1) { 0 }, 0)

    actual fun glGetActiveAttrib(program: GLProgram, index: Int): String =
        GLES20.glGetActiveAttrib(program.program, index, IntArray(1) { 0 }, 0, IntArray(1) { 0 }, 0)

    actual fun glShaderSource(
        shader: GLShader, source: String
    ) = GLES20.glShaderSource(
        shader.glHandle, source
    )

    actual fun glCompileShader(shader: GLShader) =
        GLES20.glCompileShader(shader.glHandle)

    actual fun glEnableVertexAttribArray(index: Int) =
        GLES20.glEnableVertexAttribArray(index)

    actual fun glGetUniformLocation(program: GLProgram, name: String): GLUniformLocation =
        com.zakgof.korender.impl.gl.GLUniformLocation(
            GLES20.glGetUniformLocation(
                program.program,
                name
            )
        )

    actual fun glGetAttribLocation(program: GLProgram, name: String) =
        GLES20.glGetAttribLocation(program.program, name)

    actual fun glUniform1i(location: GLUniformLocation, v0: Int) =
        GLES20.glUniform1i(location.glHandle, v0)

    actual fun glUniform1iv(location: GLUniformLocation, vararg v0: Int) =
        GLES20.glUniform1iv(location.glHandle, v0.size, v0, 0)

    actual fun glUniform1f(location: GLUniformLocation, v0: Float) =
        GLES20.glUniform1f(location.glHandle, v0)

    actual fun glUniform2f(location: GLUniformLocation, v0: Float, v1: Float) =
        GLES20.glUniform2f(location.glHandle, v0, v1)

    actual fun glUniform3f(location: GLUniformLocation, v0: Float, v1: Float, v2: Float) =
        GLES20.glUniform3f(location.glHandle, v0, v1, v2)

    actual fun glUniform4f(location: GLUniformLocation, v0: Float, v1: Float, v2: Float, v3: Float) =
        GLES20.glUniform4f(location.glHandle, v0, v1, v2, v3)

    actual fun glUniform1fv(location: GLUniformLocation, value: FloatArray)=
        GLES20.glUniform1fv(location.glHandle, value.size, value, 0)

    actual fun glUniform3fv(location: GLUniformLocation, value: FloatArray) =
        GLES20.glUniform3fv(location.glHandle, value.size / 3, value, 0)

    actual fun glUniform4fv(location: GLUniformLocation, value: FloatArray) =
        GLES20.glUniform4fv(location.glHandle, value.size / 4, value, 0)

    actual fun glUniformMatrix2fv(location: GLUniformLocation, transpose: Boolean, value: FloatArray) =
        GLES20.glUniformMatrix2fv(location.glHandle, 1, transpose, value, 0)

    actual fun glUniformMatrix3fv(location: GLUniformLocation, transpose: Boolean, value: FloatArray) =
        GLES20.glUniformMatrix3fv(location.glHandle, value.size / 9, transpose, value, 0)

    actual fun glUniformMatrix4fv(location: GLUniformLocation, transpose: Boolean, value: FloatArray) =
        GLES20.glUniformMatrix4fv(location.glHandle, value.size / 16, transpose, value, 0)

    actual fun glVertexAttribPointer(
        index: Int,
        size: Int,
        type: Int,
        normalized: Boolean,
        stride: Int,
        pointer: Int
    ) =
        GLES20.glVertexAttribPointer(index, size, type, normalized, stride, pointer)

    actual fun glVertexAttribIPointer(
        index: Int,
        size: Int,
        type: Int,
        stride: Int,
        pointer: Int
    ) =
        GLES30.glVertexAttribIPointer(index, size, type, stride, pointer)

    actual fun glGetShaderi(shader: GLShader, pname: Int): Int =
        intViaArray { GLES20.glGetShaderiv(shader.glHandle, pname, it, 0) }

    actual fun glDeleteShader(shader: GLShader) =
        GLES20.glDeleteShader(shader.glHandle)

    actual fun glDeleteProgram(program: GLProgram) =
        GLES20.glDeleteProgram(program.program)

    actual fun glGenerateMipmap(target: Int) =
        GLES20.glGenerateMipmap(target)

    actual fun glGenFramebuffers(): GLFrameBuffer =
        com.zakgof.korender.impl.gl.GLFrameBuffer(intViaArray {
            GLES20.glGenFramebuffers(
                1,
                it,
                0
            )
        })

    actual fun glFramebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: GLTexture, level: Int) =
        GLES20.glFramebufferTexture2D(target, attachment, textarget, texture.texture, level)

    actual fun glDeleteFramebuffers(framebuffer: GLFrameBuffer) =
        GLES20.glDeleteFramebuffers(1, intArrayOf(framebuffer.frameBuffer), 0)

    actual fun glBindFramebuffer(target: Int, framebuffer: GLFrameBuffer?) =
        GLES20.glBindFramebuffer(target, framebuffer?.frameBuffer ?: 0)

    actual fun glCheckFramebufferStatus(target: Int) =
        GLES20.glCheckFramebufferStatus(target)

    private fun intViaArray(function: (IntArray) -> Unit) = IntArray(1).apply(function)[0]

    actual fun glBindVertexArray(vertexArray: GLVertexArray?) =
        GLES30.glBindVertexArray(vertexArray?.glHandle ?: 0)

    actual fun glBindAttribLocation(program: GLProgram, index: Int, attr: String) =
        GLES20.glBindAttribLocation(program.program, index, attr)

    actual fun glGenVertexArrays(): GLVertexArray =
        GLVertexArray(IntArray(1).also {
            GLES30.glGenVertexArrays(1, it, 0)
        }[0])

    actual fun glDeleteVertexArrays(vertexArray: GLVertexArray) =
        GLES30.glDeleteVertexArrays(1, intArrayOf(vertexArray.glHandle), 0)

    actual fun glDrawBuffers(vararg targets: Int) =
        GLES30.glDrawBuffers(targets.size, targets, 0)

}
