package com.zakgof.korender.impl.gl

import android.opengl.GLES30
import com.zakgof.korender.impl.buffer.NativeBuffer
import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.impl.gl.GLConstants.GL_MAX_TEXTURE_MAX_ANISOTROPY
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_MAX_ANISOTROPY

actual object GL {

    actual val shaderEnv = "GLES"

    actual fun glDrawElements(mode: Int, count: Int, type: Int, indices: Int) =
        GLES30.glDrawElements(mode, count, type, indices)

    actual fun glDrawArrays(mode: Int, starting: Int, count: Int) =
        GLES30.glDrawArrays(mode, starting, count)

    actual fun glEnable(target: Int) = GLES30.glEnable(target)

    actual fun glDisable(target: Int) = GLES30.glDisable(target)

    actual fun glBindTexture(target: Int, texture: GLTexture?) =
        GLES30.glBindTexture(target, texture?.texture ?: 0)

    actual fun glTexParameterf(target: Int, pname: Int, param: Float) =
        GLES30.glTexParameterf(target, pname, param)

    actual fun glDeleteTextures(texture: GLTexture) =
        GLES30.glDeleteTextures(1, intArrayOf(texture.texture), 0)

    actual fun glPixelStorei(pname: Int, param: Int) =
        GLES30.glPixelStorei(pname, param)

    actual fun glGenTextures() =
        GLTexture(IntArray(1).also {
            GLES30.glGenTextures(1, it, 0)
        }[0])

    actual fun glBlendFunc(sfactor: Int, dfactor: Int) =
        GLES30.glBlendFunc(sfactor, dfactor)

    actual fun glDepthFunc(func: Int) =
        GLES30.glDepthFunc(func)

    actual fun glDepthMask(flag: Boolean) =
        GLES30.glDepthMask(flag)

    actual fun glCullFace(mode: Int) =
        GLES30.glCullFace(mode)

    actual fun glTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, format: Int, type: Int, buffer: NativeBuffer?) =
        GLES30.glTexImage2D(target, level, internalformat, width, height, border, format, type, buffer?.byteBuffer)

    actual fun glGetFloatv(pname: Int): Float? =
        FloatArray(1).also { GLES30.glGetFloatv(pname, it, 0) }[0]

    actual fun glGetError(): Int = GLES30.glGetError()

    actual fun glClear(mask: Int) = GLES30.glClear(mask)

    actual fun glClearColor(fl: Float, fl1: Float, fl2: Float, fl3: Float) =
        GLES30.glClearColor(fl, fl1, fl2, fl3)

    actual fun glViewport(x: Int, y: Int, w: Int, h: Int) = GLES30.glViewport(x, y, w, h)

    actual fun glTexParameteri(target: Int, pname: Int, param: Int) =
        GLES30.glTexParameteri(target, pname, param)

    actual fun glTexParameterfv(target: Int, pname: Int, param: FloatArray) =
        GLES30.glTexParameterfv(target, pname, param, 0)

    actual fun glActiveTexture(texture: Int) = GLES30.glActiveTexture(texture)

    actual fun glBindBuffer(target: Int, buffer: GLBuffer) =
        GLES30.glBindBuffer(target, buffer.buffer)

    actual fun glBufferData(target: Int, data: NativeByteBuffer, usage: Int) =
        GLES30.glBufferData(target, data.byteBuffer.remaining(), data.byteBuffer, usage)

    actual fun glGenBuffers() =
        GLBuffer(IntArray(1).also { GLES30.glGenBuffers(1, it, 0) }[0])

    actual fun glDeleteBuffers(buffer: GLBuffer) =
        GLES30.glDeleteBuffers(1, intArrayOf(buffer.buffer), 0)

    actual fun glCreateProgram() =
        GLProgram(GLES30.glCreateProgram())

    actual fun glCreateShader(type: Int) =
        GLShader(GLES30.glCreateShader(type))

    actual fun glAttachShader(program: GLProgram, shader: GLShader) =
        GLES30.glAttachShader(program.program, shader.glHandle)

    actual fun glLinkProgram(program: GLProgram) =
        GLES30.glLinkProgram(program.program)

    actual fun glUseProgram(program: GLProgram?) =
        GLES30.glUseProgram(program?.program ?: 0)

    actual fun glValidateProgram(program: GLProgram) =
        GLES30.glValidateProgram(program.program)

    actual fun glGetProgrami(program: GLProgram, pname: Int): Int =
        intViaArray { GLES30.glGetProgramiv(program.program, pname, it, 0) }

    actual fun glGetShaderInfoLog(shader: GLShader): String =
        GLES30.glGetShaderInfoLog(shader.glHandle)

    actual fun glGetProgramInfoLog(program: GLProgram): String =
        GLES30.glGetProgramInfoLog(program.program)

    actual fun glGetActiveUniform(program: GLProgram, index: Int): String =
        GLES30.glGetActiveUniform(program.program, index, IntArray(1), 0, IntArray(1), 0)

    actual fun glGetActiveAttrib(program: GLProgram, index: Int): String =
        GLES30.glGetActiveAttrib(program.program, index, IntArray(1), 0, IntArray(1), 0)

    actual fun glShaderSource(shader: GLShader, source: String) =
        GLES30.glShaderSource(shader.glHandle, source)

    actual fun glCompileShader(shader: GLShader) =
        GLES30.glCompileShader(shader.glHandle)

    actual fun glEnableVertexAttribArray(index: Int) =
        GLES30.glEnableVertexAttribArray(index)

    actual fun glGetUniformLocation(program: GLProgram, name: String) =
        GLUniformLocation(GLES30.glGetUniformLocation(program.program, name))

    actual fun glGetAttribLocation(program: GLProgram, name: String) =
        GLES30.glGetAttribLocation(program.program, name)

    actual fun glUniform1i(location: GLUniformLocation, v0: Int) =
        GLES30.glUniform1i(location.glHandle, v0)

    actual fun glUniform1iv(location: GLUniformLocation, vararg v0: Int) =
        GLES30.glUniform1iv(location.glHandle, v0.size, v0, 0)

    actual fun glUniform1f(location: GLUniformLocation, v0: Float) =
        GLES30.glUniform1f(location.glHandle, v0)

    actual fun glUniform2f(location: GLUniformLocation, v0: Float, v1: Float) =
        GLES30.glUniform2f(location.glHandle, v0, v1)

    actual fun glUniform3f(location: GLUniformLocation, v0: Float, v1: Float, v2: Float) =
        GLES30.glUniform3f(location.glHandle, v0, v1, v2)

    actual fun glUniform4f(location: GLUniformLocation, v0: Float, v1: Float, v2: Float, v3: Float) =
        GLES30.glUniform4f(location.glHandle, v0, v1, v2, v3)

    actual fun glUniform1fv(location: GLUniformLocation, value: FloatArray) =
        GLES30.glUniform1fv(location.glHandle, value.size, value, 0)

    actual fun glUniform3fv(location: GLUniformLocation, value: FloatArray) =
        GLES30.glUniform3fv(location.glHandle, value.size / 3, value, 0)

    actual fun glUniform4fv(location: GLUniformLocation, value: FloatArray) =
        GLES30.glUniform4fv(location.glHandle, value.size / 4, value, 0)

    actual fun glUniformMatrix2fv(location: GLUniformLocation, transpose: Boolean, value: FloatArray) =
        GLES30.glUniformMatrix2fv(location.glHandle, 1, transpose, value, 0)

    actual fun glUniformMatrix3fv(location: GLUniformLocation, transpose: Boolean, value: FloatArray) =
        GLES30.glUniformMatrix3fv(location.glHandle, value.size / 9, transpose, value, 0)

    actual fun glUniformMatrix4fv(location: GLUniformLocation, transpose: Boolean, value: FloatArray) =
        GLES30.glUniformMatrix4fv(location.glHandle, value.size / 16, transpose, value, 0)

    actual fun glVertexAttribPointer(index: Int, size: Int, type: Int, normalized: Boolean, stride: Int, pointer: Int) =
        GLES30.glVertexAttribPointer(index, size, type, normalized, stride, pointer)

    actual fun glVertexAttribIPointer(index: Int, size: Int, type: Int, stride: Int, pointer: Int) =
        GLES30.glVertexAttribIPointer(index, size, type, stride, pointer)

    actual fun glGetShaderi(shader: GLShader, pname: Int): Int =
        intViaArray { GLES30.glGetShaderiv(shader.glHandle, pname, it, 0) }

    actual fun glDeleteShader(shader: GLShader) =
        GLES30.glDeleteShader(shader.glHandle)

    actual fun glDeleteProgram(program: GLProgram) =
        GLES30.glDeleteProgram(program.program)

    actual fun glGenerateMipmap(target: Int) =
        GLES30.glGenerateMipmap(target)

    actual fun glGenFramebuffers(): GLFrameBuffer =
        GLFrameBuffer(intViaArray { GLES30.glGenFramebuffers(1, it, 0) })

    actual fun glFramebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: GLTexture, level: Int) =
        GLES30.glFramebufferTexture2D(target, attachment, textarget, texture.texture, level)

    actual fun glDeleteFramebuffers(framebuffer: GLFrameBuffer) =
        GLES30.glDeleteFramebuffers(1, intArrayOf(framebuffer.frameBuffer), 0)

    actual fun glBindFramebuffer(target: Int, framebuffer: GLFrameBuffer?) =
        GLES30.glBindFramebuffer(target, framebuffer?.frameBuffer ?: 0)

    actual fun glCheckFramebufferStatus(target: Int) =
        GLES30.glCheckFramebufferStatus(target)

    actual fun glBindVertexArray(vertexArray: GLVertexArray?) =
        GLES30.glBindVertexArray(vertexArray?.glHandle ?: 0)

    actual fun glBindAttribLocation(program: GLProgram, index: Int, attr: String) =
        GLES30.glBindAttribLocation(program.program, index, attr)

    actual fun glGenVertexArrays(): GLVertexArray =
        GLVertexArray(IntArray(1).also { GLES30.glGenVertexArrays(1, it, 0) }[0])

    actual fun glDeleteVertexArrays(vertexArray: GLVertexArray) =
        GLES30.glDeleteVertexArrays(1, intArrayOf(vertexArray.glHandle), 0)

    actual fun glDrawBuffers(vararg targets: Int) =
        GLES30.glDrawBuffers(targets.size, targets, 0)

    actual fun glGetTexImage(tex: Int, level: Int, format: Int, type: Int, pixels: NativeByteBuffer) {
        // TODO rewrite tex fetch using FB
        throw NotImplementedError()
    }

    actual fun glClearDepth(fl: Float) =
        GLES30.glClearDepthf(fl)

    actual fun glReadPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, data: NativeByteBuffer) =
        GLES30.glReadPixels(x, y, width, height, format, type, data.byteBuffer)

    actual fun glDrawElementsInstanced(mode: Int, count: Int, type: Int, indices: Int, instances: Int) =
        GLES30.glDrawElementsInstanced(mode, count, type, indices, instances)

    actual fun glDrawArraysInstanced(mode: Int, starting: Int, count: Int, instances: Int) =
        GLES30.glDrawArraysInstanced(mode, starting, count, instances)

    actual fun glVertexAttribDivisor(index: Int, divisor: Int) =
        GLES30.glVertexAttribDivisor(index, divisor)

    actual fun glGetMaxTextureMaxAnisotropyConstant() = GL_MAX_TEXTURE_MAX_ANISOTROPY

    actual fun glGetTextureMaxAnisotropyConstant() = GL_TEXTURE_MAX_ANISOTROPY

    private fun intViaArray(function: (IntArray) -> Unit) =
        IntArray(1).apply(function)[0]
}

