package com.zakgof.korender.gl

import com.zakgof.korender.buffer.BufferData
import com.zakgof.korender.buffer.Byter
import com.zakgof.korender.buffer.Floater
import com.zakgof.korender.buffer.Inter
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30

actual object GL {

    actual val shaderEnv = "OPENGL"

    actual fun glDrawElements(mode: Int, count: Int, type: Int, indices: Int) =
        GL11.glDrawElements(mode, count, type, indices.toLong())

    actual fun glEnable(target: Int) = GL11.glEnable(target)
    actual fun glDisable(target: Int) = GL11.glDisable(target)
    actual fun glBindTexture(target: Int, texture: GLTexture) =
        GL11.glBindTexture(target, texture.glHandle)

    actual fun glTexParameterf(target: Int, pname: Int, param: Float) =
        GL11.glTexParameterf(target, pname, param)

    actual fun glDeleteTextures(texture: GLTexture) = GL11.glDeleteTextures(texture.glHandle)
    actual fun glPixelStorei(pname: Int, param: Int) = GL11.glPixelStorei(pname, param)
    actual fun glGenTextures(): GLTexture = GLTexture(GL11.glGenTextures())
    actual fun glBlendFunc(sfactor: Int, dfactor: Int) = GL11.glBlendFunc(sfactor, dfactor)
    actual fun glDepthFunc(func: Int) = GL11.glDepthFunc(func)
    actual fun glDepthMask(flag: Boolean) = GL11.glDepthMask(flag)
    actual fun glCullFace(mode: Int) = GL11.glCullFace(mode)
    actual fun glTexImage2D(
        target: Int,
        level: Int,
        internalformat: Int,
        width: Int,
        height: Int,
        border: Int,
        format: Int,
        type: Int,
        pixels: Byter?
    ) = GL11.glTexImage2D(
        target, level, internalformat, width, height, border, format, type, pixels?.byteBuffer
    )

    actual fun glGetFloatv(pname: Int): Float? {
        val fa = FloatArray(1)
        GL11.glGetFloatv(pname, fa)
        return fa[0]
    }

    actual fun glGetError(): Int = GL11.glGetError()
    actual fun glClear(mask: Int) = GL11.glClear(mask)
    actual fun glClearColor(fl: Float, fl1: Float, fl2: Float, fl3: Float) =
        GL11.glClearColor(fl, fl1, fl2, fl3)

    actual fun glViewport(x: Int, y: Int, w: Int, h: Int) = GL11.glViewport(x, y, w, h)
    actual fun glTexParameteri(target: Int, pname: Int, param: Int) =
        GL11.glTexParameteri(target, pname, param)

    actual fun glTexParameterfv(target: Int, pname: Int, param: FloatArray) =
        GL11.glTexParameterfv(target, pname, param)

    actual fun glActiveTexture(texture: Int) = GL13.glActiveTexture(texture)
    actual fun glBindBuffer(target: Int, buffer: GLBuffer) =
        GL15.glBindBuffer(target, buffer.glHandle)

    actual fun glGenBuffers() = GLBuffer(GL15.glGenBuffers())
    actual fun glDeleteBuffers(buffer: GLBuffer) = GL15.glDeleteBuffers(buffer.glHandle)
    actual fun glCreateProgram() = GLProgram(GL20.glCreateProgram())
    actual fun glCreateShader(type: Int) = GLShader(GL20.glCreateShader(type))
    actual fun glAttachShader(program: GLProgram, shader: GLShader) =
        GL20.glAttachShader(program.glHandle, shader.glHandle)

    actual fun glLinkProgram(program: GLProgram) = GL20.glLinkProgram(program.glHandle)
    actual fun glUseProgram(program: GLProgram?) = GL20.glUseProgram(program?.glHandle ?: 0)
    actual fun glValidateProgram(program: GLProgram) = GL20.glValidateProgram(program.glHandle)
    actual fun glGetProgrami(program: GLProgram, pname: Int) =
        GL20.glGetProgrami(program.glHandle, pname)

    actual fun glGetShaderInfoLog(shader: GLShader) = GL20.glGetShaderInfoLog(shader.glHandle)
    actual fun glGetProgramInfoLog(program: GLProgram) = GL20.glGetProgramInfoLog(program.glHandle)
    actual fun glGetProgramiv(program: GLProgram, pname: Int, params: Inter) =
        GL20.glGetProgramiv(program.glHandle, pname, params.intBuffer)

    actual fun glGetActiveUniform(
        program: GLProgram, index: Int, size: Inter, type: Inter
    ) = GL20.glGetActiveUniform(program.glHandle, index, size.intBuffer, type.intBuffer)

    actual fun glGetActiveAttrib(
        program: GLProgram, index: Int, size: Inter, type: Inter
    ) = GL20.glGetActiveAttrib(program.glHandle, index, size.intBuffer, type.intBuffer)

    actual fun glShaderSource(shader: GLShader, source: String) =
        GL20.glShaderSource(shader.glHandle, source)

    actual fun glCompileShader(shader: GLShader) = GL20.glCompileShader(shader.glHandle)
    actual fun glGetShaderiv(shader: GLShader, pname: Int, params: Inter) =
        GL20.glGetShaderiv(shader.glHandle, pname, params.intBuffer)

    actual fun glEnableVertexAttribArray(index: Int) = GL20.glEnableVertexAttribArray(index)
    actual fun glGetUniformLocation(program: GLProgram, name: String) =
        GLUniformLocation(GL20.glGetUniformLocation(program.glHandle, name))

    actual fun glGetAttribLocation(program: GLProgram, name: String) =
        GL20.glGetAttribLocation(program.glHandle, name)

    actual fun glUniform1i(location: GLUniformLocation, v0: Int) =
        GL20.glUniform1i(location.glHandle, v0)

    actual fun glUniform1f(location: GLUniformLocation, v0: Float) =
        GL20.glUniform1f(location.glHandle, v0)

    actual fun glUniform2f(location: GLUniformLocation, v0: Float, v1: Float) =
        GL20.glUniform2f(location.glHandle, v0, v1)

    actual fun glUniform3f(location: GLUniformLocation, v0: Float, v1: Float, v2: Float) =
        GL20.glUniform3f(location.glHandle, v0, v1, v2)

    actual fun glUniform4f(
        location: GLUniformLocation,
        v0: Float,
        v1: Float,
        v2: Float,
        v3: Float
    ) =
        GL20.glUniform4f(location.glHandle, v0, v1, v2, v3)

    actual fun glUniformMatrix2fv(location: GLUniformLocation, transpose: Boolean, value: Floater) =
        GL20.glUniformMatrix2fv(location.glHandle, transpose, value.floatBuffer)

    actual fun glUniformMatrix3fv(location: GLUniformLocation, transpose: Boolean, value: Floater) =
        GL20.glUniformMatrix3fv(location.glHandle, transpose, value.floatBuffer)

    actual fun glUniformMatrix4fv(location: GLUniformLocation, transpose: Boolean, value: Floater) =
        GL20.glUniformMatrix4fv(location.glHandle, transpose, value.floatBuffer)

    actual fun glVertexAttribPointer(
        index: Int, size: Int, type: Int, normalized: Boolean, stride: Int, pointer: Int
    ) = GL20.glVertexAttribPointer(index, size, type, normalized, stride, pointer.toLong())

    actual fun glGetShaderi(shader: GLShader, pname: Int) =
        GL20.glGetShaderi(shader.glHandle, pname)

    actual fun glDeleteShader(shader: GLShader) = GL20.glDeleteShader(shader.glHandle)
    actual fun glDeleteProgram(program: GLProgram) = GL20.glDeleteProgram(program.glHandle)

    actual fun glGenerateMipmap(target: Int) = GL30.glGenerateMipmap(target)
    actual fun glGenFramebuffers(): GLFrameBuffer = GLFrameBuffer(GL30.glGenFramebuffers())
    actual fun glFramebufferTexture2D(
        target: Int, attachment: Int, textarget: Int, texture: GLTexture, level: Int
    ) = GL30.glFramebufferTexture2D(target, attachment, textarget, texture.glHandle, level)

    actual fun glDeleteFramebuffers(framebuffer: GLFrameBuffer) =
        GL30.glDeleteFramebuffers(framebuffer.glHandle)

    actual fun glBindFramebuffer(target: Int, framebuffer: GLFrameBuffer?) =
        GL30.glBindFramebuffer(target, framebuffer?.glHandle ?: 0)

    actual fun glCheckFramebufferStatus(target: Int) = GL30.glCheckFramebufferStatus(target)

    actual fun glBindVertexArray(vertexArray: GLVertexArray?) =
        GL30.glBindVertexArray(vertexArray?.glHandle ?: 0)

    actual fun glBindAttribLocation(program: GLProgram, index: Int, attr: String) =
        GL20.glBindAttribLocation(program.glHandle, index, attr)

    actual fun glGenVertexArrays(): GLVertexArray = GLVertexArray(GL30.glGenVertexArrays())

    actual fun glDeleteVertexArrays(vertexArray: GLVertexArray) =
        GL30.glDeleteVertexArrays(vertexArray.glHandle)

    actual fun glBufferData(target: Int, data: BufferData<out Any>, usage: Int) =
        GL20.glBufferData(target, data.byteBuffer, usage)
}
