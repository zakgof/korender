package com.zakgof.korender.impl.gl

import com.zakgof.korender.KorenderException
import com.zakgof.korender.WebGL2RenderingContext
import com.zakgof.korender.impl.buffer.NativeBuffer
import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.impl.gl.GLConstants.GL_MAX_TEXTURE_MAX_ANISOTROPY
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_MAX_ANISOTROPY
import com.zakgof.korender.maxTexAniso
import com.zakgof.korender.texAniso
import org.khronos.webgl.Uint32Array
import org.khronos.webgl.get
import org.khronos.webgl.toFloat32Array
import org.khronos.webgl.toInt32Array

actual object GL {

    // TODO: Check with multiple Korender windows
    internal var gl: WebGL2RenderingContext? = null

    actual val shaderEnv = "WEBGL"

    actual fun glDrawElements(mode: Int, count: Int, type: Int, indices: Int) =
        gl!!.drawElements(mode, count, type, indices)

    actual fun glDrawElementsInstanced(mode: Int, count: Int, type: Int, indices: Int, instances: Int) =
        gl!!.drawElementsInstanced(mode, count, type, indices, instances)

    actual fun glDrawArrays(mode: Int, starting: Int, count: Int) =
        gl!!.drawArrays(mode, starting, count)

    actual fun glDrawArraysInstanced(mode: Int, starting: Int, count: Int, instances: Int) =
        gl!!.drawArraysInstanced(mode, starting, count, instances)

    actual fun glEnable(target: Int) =
        gl!!.enable(target)

    actual fun glDisable(target: Int) =
        gl!!.disable(target)

    actual fun glBindTexture(target: Int, texture: GLTexture?) =
        gl!!.bindTexture(target, texture?.texture)

    actual fun glTexParameterf(target: Int, pname: Int, param: Float) =
        gl!!.texParameterf(target, pname, param)

    actual fun glDeleteTextures(texture: GLTexture) =
        gl!!.deleteTexture(texture.texture)

    actual fun glPixelStorei(pname: Int, param: Int) =
        gl!!.pixelStorei(param, param)

    actual fun glGenTextures(): GLTexture =
        com.zakgof.korender.impl.gl.GLTexture(
            gl!!.createTexture() ?: throw KorenderException("Failed to create WebGL texture")
        )

    actual fun glBlendFunc(sfactor: Int, dfactor: Int) =
        gl!!.blendFunc(sfactor, dfactor)

    actual fun glDepthFunc(func: Int) =
        gl!!.depthFunc(func)

    actual fun glDepthMask(flag: Boolean) =
        gl!!.depthMask(flag)

    actual fun glCullFace(mode: Int) =
        gl!!.cullFace(mode)

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
    ) = gl!!.texImage2D(
        target, level, internalformat, width, height, border, format, type, buffer?.array
    )

    actual fun glTexSubImage2D(target: Int, level: Int, x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, buffer: NativeBuffer) =
        gl!!.texSubImage2D(target, level, x, y, width, height, format, type, buffer.array)

    actual fun glGetTexImage(tex: Int, level: Int, format: Int, type: Int, pixels: NativeByteBuffer) =
        gl!!.getTexImage(tex, level, format, type, pixels.array)

    actual fun glGetFloatv(pname: Int): Float? {
        val paramValue = gl!!.getParameter(pname)
        return (paramValue as JsNumber?)?.toDouble()?.toFloat()
    }

    actual fun glGetError() = gl!!.getError()

    actual fun glClear(mask: Int) = gl!!.clear(mask)

    actual fun glViewport(x: Int, y: Int, w: Int, h: Int) =
        gl!!.viewport(x, y, w, h)

    actual fun glTexParameteri(target: Int, pname: Int, param: Int) =
        gl!!.texParameteri(target, pname, param)

    // TODO missing
    actual fun glTexParameterfv(target: Int, pname: Int, param: FloatArray) {}

    actual fun glClearColor(fl: Float, fl1: Float, fl2: Float, fl3: Float) =
        gl!!.clearColor(fl, fl1, fl2, fl3)

    actual fun glClearDepth(fl: Float) =
        gl!!.clearDepth(fl)

    actual fun glActiveTexture(texture: Int) = gl!!.activeTexture(texture)

    actual fun glBindBuffer(target: Int, buffer: GLBuffer) =
        gl!!.bindBuffer(target, buffer.buffer)

    actual fun glGenBuffers(): GLBuffer =
        GLBuffer(gl!!.createBuffer() ?: throw KorenderException("Failed to create WebGL buffer"))

    actual fun glGenVertexArrays(): GLVertexArray =
        GLVertexArray(
            gl!!.createVertexArray()
                ?: throw KorenderException("Failed to create WebGL vertex array")
        )

    actual fun glDeleteBuffers(buffer: GLBuffer) =
        gl!!.deleteBuffer(buffer.buffer)

    actual fun glDeleteVertexArrays(vertexArray: GLVertexArray) =
        gl!!.deleteVertexArray(vertexArray.vertexArray)

    actual fun glCreateProgram(): GLProgram =
        com.zakgof.korender.impl.gl.GLProgram(
            gl!!.createProgram() ?: throw KorenderException("Failed to create WebGL program")
        )

    actual fun glCreateShader(type: Int): GLShader =
        com.zakgof.korender.impl.gl.GLShader(
            gl!!.createShader(type) ?: throw KorenderException("Failed to create WebGL shader")
        )

    actual fun glAttachShader(program: GLProgram, shader: GLShader) =
        gl!!.attachShader(program.program, shader.shader)

    actual fun glLinkProgram(program: GLProgram) =
        gl!!.linkProgram(program.program)

    actual fun glUseProgram(program: GLProgram?) =
        gl!!.useProgram(program?.program)

    actual fun glValidateProgram(program: GLProgram) =
        gl!!.validateProgram(program.program)

    actual fun glGetProgrami(program: GLProgram, pname: Int): Int =
        boolOrInt(gl!!.getProgramParameter(program.program, pname))

    actual fun glGetShaderInfoLog(shader: GLShader): String =
        gl!!.getShaderInfoLog(shader.shader) ?: ""

    actual fun glGetProgramInfoLog(program: GLProgram): String =
        gl!!.getProgramInfoLog(program.program) ?: ""

    actual fun glGetActiveUniform(program: GLProgram, index: Int) =
        gl!!.getActiveUniform(program.program, index)!!.name

    actual fun glGetActiveAttrib(program: GLProgram, index: Int) =
        gl!!.getActiveAttrib(program.program, index)!!.name

    actual fun glShaderSource(shader: GLShader, source: String) =
        gl!!.shaderSource(shader.shader, source)

    actual fun glCompileShader(shader: GLShader) =
        gl!!.compileShader(shader.shader)

    actual fun glEnableVertexAttribArray(index: Int) =
        gl!!.enableVertexAttribArray(index)

    actual fun glGetUniformLocation(program: GLProgram, name: String): GLUniformLocation? =
        gl!!.getUniformLocation(program.program, name)?.let { GLUniformLocation(it) }

    actual fun glGetAttribLocation(program: GLProgram, name: String): Int =
        gl!!.getAttribLocation(program.program, name)

    actual fun glUniform1i(location: GLUniformLocation, v0: Int) =
        gl!!.uniform1i(location.uniformLocation, v0)

    actual fun glUniform1iv(location: GLUniformLocation, vararg v0: Int) =
        gl!!.uniform1iv(location.uniformLocation, v0.toInt32Array())

    actual fun glUniform1f(location: GLUniformLocation, v0: Float) =
        gl!!.uniform1f(location.uniformLocation, v0)

    actual fun glUniform2f(location: GLUniformLocation, v0: Float, v1: Float) =
        gl!!.uniform2f(location.uniformLocation, v0, v1)

    actual fun glUniform3f(location: GLUniformLocation, v0: Float, v1: Float, v2: Float) =
        gl!!.uniform3f(location.uniformLocation, v0, v1, v2)

    actual fun glUniform4f(location: GLUniformLocation, v0: Float, v1: Float, v2: Float, v3: Float) =
        gl!!.uniform4f(location.uniformLocation, v0, v1, v2, v3)

    actual fun glUniform1fv(location: GLUniformLocation, value: FloatArray) =
        gl!!.uniform1fv(location.uniformLocation, value.toFloat32Array())

    actual fun glUniform3fv(location: GLUniformLocation, value: FloatArray) =
        gl!!.uniform3fv(location.uniformLocation, value.toFloat32Array())

    actual fun glUniform4fv(location: GLUniformLocation, value: FloatArray) =
        gl!!.uniform4fv(location.uniformLocation, value.toFloat32Array())

    actual fun glUniformMatrix2fv(location: GLUniformLocation, transpose: Boolean, value: FloatArray) =
        gl!!.uniformMatrix2fv(location.uniformLocation, transpose, value.toFloat32Array())

    actual fun glUniformMatrix3fv(location: GLUniformLocation, transpose: Boolean, value: FloatArray) =
        gl!!.uniformMatrix3fv(location.uniformLocation, transpose, value.toFloat32Array())

    actual fun glUniformMatrix4fv(location: GLUniformLocation, transpose: Boolean, value: FloatArray) =
        gl!!.uniformMatrix4fv(location.uniformLocation, transpose, value.toFloat32Array())

    actual fun glVertexAttribPointer(
        index: Int, size: Int, type: Int, normalized: Boolean, stride: Int, pointer: Int
    ) = gl!!.vertexAttribPointer(index, size, type, normalized, stride, pointer)

    actual fun glVertexAttribIPointer(
        index: Int, size: Int, type: Int, stride: Int, pointer: Int
    ) = gl!!.vertexAttribIPointer(index, size, type, stride, pointer)

    actual fun glGetShaderi(shader: GLShader, pname: Int): Int = boolOrInt(gl!!.getShaderParameter(shader.shader, pname))

    private fun boolOrInt(value: JsAny?): Int {
        if (value is JsNumber) return value.toInt()
        if (value is JsBoolean) return if (value.toBoolean()) 1 else 0
        throw KorenderException("Unknown type for glGetShaderi result: $value")
    }

    actual fun glDeleteShader(shader: GLShader) =
        gl!!.deleteShader(shader.shader)

    actual fun glDeleteProgram(program: GLProgram) =
        gl!!.deleteProgram(program.program)

    actual fun glGenerateMipmap(target: Int) =
        gl!!.generateMipmap(target)

    actual fun glGenFramebuffers(): GLFrameBuffer =
        GLFrameBuffer(gl!!.createFramebuffer())

    actual fun glFramebufferTexture2D(
        target: Int, attachment: Int, textarget: Int, texture: GLTexture, level: Int
    ) = gl!!.framebufferTexture2D(target, attachment, textarget, texture.texture, level)

    actual fun glDeleteFramebuffers(framebuffer: GLFrameBuffer) =
        gl!!.deleteFramebuffer(framebuffer.frameBuffer)

    actual fun glBindFramebuffer(target: Int, framebuffer: GLFrameBuffer?) =
        gl!!.bindFramebuffer(target, framebuffer?.frameBuffer)

    actual fun glCheckFramebufferStatus(target: Int): Int =
        gl!!.checkFramebufferStatus(target)

    actual fun glBindVertexArray(vertexArray: GLVertexArray?) =
        gl!!.bindVertexArray(vertexArray?.vertexArray)

    actual fun glBindAttribLocation(program: GLProgram, index: Int, attr: String) =
        gl!!.bindAttribLocation(program.program, index, attr)

    actual fun glBufferData(target: Int, data: NativeByteBuffer, usage: Int) =
        gl!!.bufferData(target, data.array, usage)

    actual fun glDrawBuffers(vararg targets: Int) =
        gl!!.drawBuffers(targets.toTypedArray().map { it.toJsNumber() }.toJsArray())

    actual fun glReadPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, data: NativeByteBuffer) =
        gl!!.readPixels(x, y, width, height, format, type, data.array)

    actual fun glVertexAttribDivisor(index: Int, divisor: Int) =
        gl!!.vertexAttribDivisor(index, divisor)

    actual fun glGetMaxTextureMaxAnisotropyConstant(): Int {
        val ext = gl!!.getExtension("EXT_texture_filter_anisotropic")
        return ext?.let { maxTexAniso(ext) } ?: GL_MAX_TEXTURE_MAX_ANISOTROPY
    }

    actual fun glGetTextureMaxAnisotropyConstant(): Int {
        val ext = gl!!.getExtension("EXT_texture_filter_anisotropic")
        return ext?.let { texAniso(ext) } ?: GL_TEXTURE_MAX_ANISOTROPY
    }

    actual fun glGetUniformBlockIndex(program: GLProgram, name: String): Int =
        gl!!.getUniformBlockIndex(program.program, name)

    actual fun glGetActiveUniformBlockiv(program: GLProgram, blockIndex: Int, param: Int, paramValues: IntArray) {
        when (val value = gl!!.getActiveUniformBlockParameter(program.program, blockIndex, param)) {
            is JsBigInt -> paramValues[0] = value.toLong().toInt()
            is JsNumber -> paramValues[0] = value.toInt()
            is Uint32Array -> for (i in 0 until value.length)
                    paramValues[i] = value[i]
            else -> throw KorenderException("Internal error [$value] ")
        }
    }

    actual fun glGetActiveUniformsiv(program: GLProgram, uniformIndices: IntArray, param: Int, paramValues: IntArray) {
        val value = gl!!.getActiveUniforms(program.program, uniformIndices.map { it.toJsNumber() }.toJsArray(), param)
        for (i in 0 until value.length)
            paramValues[i] = (value[i] as JsNumber).toInt()
    }

    actual fun glGetActiveUniformName(program: GLProgram, uniformIndex: Int) =
        gl!!.getActiveUniform(program.program, uniformIndex)!!.name

    actual fun glUniformBlockBinding(program: GLProgram, blockIndex: Int, blockBinding: Int) =
        gl!!.uniformBlockBinding(program.program, blockIndex, blockBinding)

    actual fun glBindBufferBase(target: Int, blockBinding: Int, buffer: GLBuffer) =
        gl!!.bindBufferBase(target, blockBinding, buffer.buffer)

    actual fun glBufferData(target: Int, size: Long, usage: Int) =
        gl!!.bufferData(target, size.toInt(), usage)

    actual fun glBufferSubData(target: Int, offset: Long, buffer: NativeByteBuffer) =
        gl!!.bufferSubData(target, offset.toInt(), buffer.array)

    actual fun glBindBufferRange(target: Int, blockBinding: Int, buffer: GLBuffer, shift: Int, size: Int) =
        gl!!.bindBufferRange(target, blockBinding, buffer.buffer, shift, size)

    actual fun glGetInteger(pname: Int) =
        (gl!!.getParameter(pname) as JsNumber).toInt()
}