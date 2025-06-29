package com.zakgof.korender.impl.gl

import com.zakgof.korender.impl.buffer.NativeBuffer
import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.impl.gl.GLConstants.GL_MAX_TEXTURE_MAX_ANISOTROPY
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_MAX_ANISOTROPY
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL33
import java.nio.ByteBuffer

actual object GL {

    actual val shaderEnv = "OPENGL"

    actual fun glDrawElements(mode: Int, count: Int, type: Int, indices: Int) =
        GL30.glDrawElements(mode, count, type, indices.toLong())

    actual fun glDrawElementsInstanced(mode: Int, count: Int, type: Int, indices: Int, instances: Int) =
        GL33.glDrawElementsInstanced(mode, count, type, indices.toLong(), instances)

    actual fun glDrawArrays(mode: Int, starting: Int, count: Int) =
        GL30.glDrawArrays(mode, starting, count)

    actual fun glDrawArraysInstanced(mode: Int, starting: Int, count: Int, instances: Int) =
        GL33.glDrawArraysInstanced(mode, starting, count, instances)

    actual fun glEnable(target: Int) =
        GL30.glEnable(target)

    // actual fun glPolygonMode(sides: Int, mode: Int) =
    //  GL30.glPolygonMode(sides, mode)

    actual fun glDisable(target: Int) =
        GL30.glDisable(target)

    actual fun glBindTexture(target: Int, texture: GLTexture?) =
        GL30.glBindTexture(target, texture?.glHandle ?: 0)

    actual fun glGetTexImage(tex: Int, level: Int, format: Int, type: Int, pixels: NativeByteBuffer) =
        GL30.glGetTexImage(tex, level, format, type, pixels.byteBuffer)

    actual fun glTexParameterf(target: Int, pname: Int, param: Float) =
        GL30.glTexParameterf(target, pname, param)

    actual fun glDeleteTextures(texture: GLTexture) =
        GL30.glDeleteTextures(texture.glHandle)

    actual fun glPixelStorei(pname: Int, param: Int) =
        GL30.glPixelStorei(pname, param)

    actual fun glGenTextures(): GLTexture =
        GLTexture(GL30.glGenTextures())

    actual fun glBlendFunc(sfactor: Int, dfactor: Int) =
        GL30.glBlendFunc(sfactor, dfactor)

    actual fun glDepthFunc(func: Int) =
        GL30.glDepthFunc(func)

    actual fun glDepthMask(flag: Boolean) =
        GL30.glDepthMask(flag)

    actual fun glCullFace(mode: Int) =
        GL30.glCullFace(mode)

    actual fun glTexImage2D(
        target: Int,
        level: Int,
        internalformat: Int,
        width: Int,
        height: Int,
        border: Int,
        format: Int,
        type: Int,
        buffer: NativeBuffer?
    ) = GL30.glTexImage2D(target, level, internalformat, width, height, border, format, type, buffer?.byteBuffer)

    actual fun glTexSubImage2D(
        target: Int,
        level: Int,
        x: Int, y: Int,
        width: Int,
        height: Int,
        format: Int,
        type: Int,
        buffer: NativeBuffer
    ) = GL30.glTexSubImage2D(target, level, x, y, width, height, format, type, buffer.byteBuffer)

    actual fun glGetFloatv(pname: Int): Float? =
        FloatArray(1).apply { GL30.glGetFloatv(pname, this) }[0]

    actual fun glGetError(): Int =
        GL30.glGetError()

    actual fun glClear(mask: Int) =
        GL30.glClear(mask)

    actual fun glClearColor(fl: Float, fl1: Float, fl2: Float, fl3: Float) =
        GL30.glClearColor(fl, fl1, fl2, fl3)

    actual fun glClearDepth(fl: Float) =
        GL30.glClearDepth(fl.toDouble())

    actual fun glViewport(x: Int, y: Int, w: Int, h: Int) =
        GL30.glViewport(x, y, w, h)

    actual fun glTexParameteri(target: Int, pname: Int, param: Int) =
        GL30.glTexParameteri(target, pname, param)

    actual fun glTexParameterfv(target: Int, pname: Int, param: FloatArray) =
        GL30.glTexParameterfv(target, pname, param)

    actual fun glActiveTexture(texture: Int) =
        GL30.glActiveTexture(texture)

    actual fun glBindBuffer(target: Int, buffer: GLBuffer) =
        GL30.glBindBuffer(target, buffer.glHandle)

    actual fun glGenBuffers() =
        GLBuffer(GL30.glGenBuffers())

    actual fun glDeleteBuffers(buffer: GLBuffer) =
        GL30.glDeleteBuffers(buffer.glHandle)

    actual fun glCreateProgram() =
        GLProgram(GL30.glCreateProgram())

    actual fun glCreateShader(type: Int) =
        GLShader(GL30.glCreateShader(type))

    actual fun glAttachShader(program: GLProgram, shader: GLShader) =
        GL30.glAttachShader(program.glHandle, shader.glHandle)

    actual fun glLinkProgram(program: GLProgram) =
        GL30.glLinkProgram(program.glHandle)

    actual fun glUseProgram(program: GLProgram?) =
        GL30.glUseProgram(program?.glHandle ?: 0)

    actual fun glValidateProgram(program: GLProgram) =
        GL30.glValidateProgram(program.glHandle)

    actual fun glGetProgrami(program: GLProgram, pname: Int) =
        GL30.glGetProgrami(program.glHandle, pname)

    actual fun glGetShaderInfoLog(shader: GLShader) =
        GL30.glGetShaderInfoLog(shader.glHandle)

    actual fun glGetProgramInfoLog(program: GLProgram) =
        GL30.glGetProgramInfoLog(program.glHandle)

    actual fun glGetActiveUniform(program: GLProgram, index: Int): String {
        val i1 = ByteBuffer.allocateDirect(4).asIntBuffer()
        val i2 = ByteBuffer.allocateDirect(4).asIntBuffer()
        return GL30.glGetActiveUniform(program.glHandle, index, i1, i2)
    }

    actual fun glGetActiveAttrib(program: GLProgram, index: Int): String {
        val i1 = ByteBuffer.allocateDirect(4).asIntBuffer()
        val i2 = ByteBuffer.allocateDirect(4).asIntBuffer()
        return GL30.glGetActiveAttrib(program.glHandle, index, i1, i2)
    }

    actual fun glShaderSource(shader: GLShader, source: String) =
        GL30.glShaderSource(shader.glHandle, source)

    actual fun glCompileShader(shader: GLShader) =
        GL30.glCompileShader(shader.glHandle)

    actual fun glEnableVertexAttribArray(index: Int) =
        GL30.glEnableVertexAttribArray(index)

    actual fun glVertexAttribDivisor(index: Int, divisor: Int) =
        GL33.glVertexAttribDivisor(index, divisor)

    actual fun glGetUniformLocation(program: GLProgram, name: String): GLUniformLocation? {
        val location = GL30.glGetUniformLocation(program.glHandle, name)
        return if (location < 0) null else GLUniformLocation(location)
    }

    actual fun glGetAttribLocation(program: GLProgram, name: String) =
        GL30.glGetAttribLocation(program.glHandle, name)

    actual fun glUniform1i(location: GLUniformLocation, v0: Int) =
        GL30.glUniform1i(location.glHandle, v0)

    actual fun glUniform1iv(location: GLUniformLocation, vararg v0: Int) =
        GL30.glUniform1iv(location.glHandle, v0)

    actual fun glUniform1f(location: GLUniformLocation, v0: Float) =
        GL30.glUniform1f(location.glHandle, v0)

    actual fun glUniform2f(location: GLUniformLocation, v0: Float, v1: Float) =
        GL30.glUniform2f(location.glHandle, v0, v1)

    actual fun glUniform3f(location: GLUniformLocation, v0: Float, v1: Float, v2: Float) =
        GL30.glUniform3f(location.glHandle, v0, v1, v2)

    actual fun glUniform4f(location: GLUniformLocation, v0: Float, v1: Float, v2: Float, v3: Float) =
        GL30.glUniform4f(location.glHandle, v0, v1, v2, v3)

    actual fun glUniform1fv(location: GLUniformLocation, value: FloatArray) =
        GL30.glUniform1fv(location.glHandle, value)

    actual fun glUniform3fv(location: GLUniformLocation, value: FloatArray) =
        GL30.glUniform3fv(location.glHandle, value)

    actual fun glUniform4fv(location: GLUniformLocation, value: FloatArray) =
        GL30.glUniform4fv(location.glHandle, value)

    actual fun glUniformMatrix2fv(location: GLUniformLocation, transpose: Boolean, value: FloatArray) =
        GL30.glUniformMatrix2fv(location.glHandle, transpose, value)

    actual fun glUniformMatrix3fv(location: GLUniformLocation, transpose: Boolean, value: FloatArray) =
        GL30.glUniformMatrix3fv(location.glHandle, transpose, value)

    actual fun glUniformMatrix4fv(location: GLUniformLocation, transpose: Boolean, value: FloatArray) =
        GL30.glUniformMatrix4fv(location.glHandle, transpose, value)

    actual fun glVertexAttribPointer(index: Int, size: Int, type: Int, normalized: Boolean, stride: Int, pointer: Int) =
        GL30.glVertexAttribPointer(index, size, type, normalized, stride, pointer.toLong())

    actual fun glVertexAttribIPointer(index: Int, size: Int, type: Int, stride: Int, pointer: Int) =
        GL30.glVertexAttribIPointer(index, size, type, stride, pointer.toLong())

    actual fun glGetShaderi(shader: GLShader, pname: Int) =
        GL30.glGetShaderi(shader.glHandle, pname)

    actual fun glDeleteShader(shader: GLShader) =
        GL30.glDeleteShader(shader.glHandle)

    actual fun glDeleteProgram(program: GLProgram) =
        GL30.glDeleteProgram(program.glHandle)

    actual fun glGenerateMipmap(target: Int) =
        GL30.glGenerateMipmap(target)

    actual fun glGenFramebuffers(): GLFrameBuffer =
        GLFrameBuffer(GL30.glGenFramebuffers())

    actual fun glFramebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: GLTexture, level: Int) =
        GL30.glFramebufferTexture2D(target, attachment, textarget, texture.glHandle, level)

    actual fun glDeleteFramebuffers(framebuffer: GLFrameBuffer) =
        GL30.glDeleteFramebuffers(framebuffer.glHandle)

    actual fun glBindFramebuffer(target: Int, framebuffer: GLFrameBuffer?) =
        GL30.glBindFramebuffer(target, framebuffer?.glHandle ?: 0)

    actual fun glCheckFramebufferStatus(target: Int) =
        GL30.glCheckFramebufferStatus(target)

    actual fun glBindVertexArray(vertexArray: GLVertexArray?) =
        GL30.glBindVertexArray(vertexArray?.glHandle ?: 0)

    actual fun glBindAttribLocation(program: GLProgram, index: Int, attr: String) =
        GL30.glBindAttribLocation(program.glHandle, index, attr)

    actual fun glGenVertexArrays(): GLVertexArray =
        GLVertexArray(GL30.glGenVertexArrays())

    actual fun glDeleteVertexArrays(vertexArray: GLVertexArray) =
        GL30.glDeleteVertexArrays(vertexArray.glHandle)

    actual fun glBufferData(target: Int, data: NativeByteBuffer, usage: Int) =
        GL30.glBufferData(target, data.byteBuffer, usage)

    actual fun glDrawBuffers(vararg targets: Int) =
        GL30.glDrawBuffers(targets)

    actual fun glReadPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, data: NativeByteBuffer) =
        GL30.glReadPixels(x, y, width, height, format, type, data.byteBuffer)

    actual fun glGetMaxTextureMaxAnisotropyConstant() = GL_MAX_TEXTURE_MAX_ANISOTROPY

    actual fun glGetTextureMaxAnisotropyConstant() = GL_TEXTURE_MAX_ANISOTROPY

    actual fun glGetUniformBlockIndex(program: GLProgram, name: String): Int =
        GL33.glGetUniformBlockIndex(program.glHandle, name)

    actual fun glGetActiveUniformBlockiv(program: GLProgram, blockIndex: Int, param: Int, paramValues: IntArray) =
        GL33.glGetActiveUniformBlockiv(program.glHandle, blockIndex, param, paramValues)

    actual fun glGetActiveUniformsiv(program: GLProgram, uniformIndices: IntArray, param: Int, paramValues: IntArray) =
        GL33.glGetActiveUniformsiv(program.glHandle, uniformIndices, param, paramValues)

    actual fun glGetActiveUniformName(program: GLProgram, uniformIndex: Int): String =
        GL33.glGetActiveUniformName(program.glHandle, uniformIndex)

    actual fun glUniformBlockBinding(program: GLProgram, blockIndex: Int, blockBinding: Int) =
        GL33.glUniformBlockBinding(program.glHandle, blockIndex, blockBinding)

    actual fun glBindBufferBase(target: Int, blockBinding: Int, buffer: GLBuffer) =
        GL33.glBindBufferBase(target, blockBinding, buffer.glHandle)
}
