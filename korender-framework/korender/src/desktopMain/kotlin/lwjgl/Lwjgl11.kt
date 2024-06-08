package com.zakgof.korender.lwjgl

import org.lwjgl.opengl.GL11
import java.nio.ByteBuffer

class Lwjgl11 : com.zakgof.korender.impl.gl.IGL11 {

    override val shaderEnv = "OPENGL"

    override fun glDrawElements(mode: Int, count: Int, type: Int, indices: Int) =
        GL11.glDrawElements(mode, count, type, indices.toLong())

    override fun glEnable(target: Int) = GL11.glEnable(target)

    override fun glDisable(target: Int) = GL11.glDisable(target)
    override fun glBindTexture(target: Int, texture: Int) = GL11.glBindTexture(target, texture)
    override fun glTexParameterf(target: Int, pname: Int, param: Float) = GL11.glTexParameterf(target, pname, param)
    override fun glDeleteTextures(texture: Int) = GL11.glDeleteTextures(texture)
    override fun glPixelStorei(pname: Int, param: Int) = GL11.glPixelStorei(pname, param)
    override fun glGenTextures(): Int = GL11.glGenTextures()
    override fun glBlendFunc(sfactor: Int, dfactor: Int) = GL11.glBlendFunc(sfactor, dfactor)
    override fun glDepthFunc(func: Int) = GL11.glDepthFunc(func)
    override fun glDepthMask(flag: Boolean) = GL11.glDepthMask(flag)
    override fun glCullFace(mode: Int) = GL11.glCullFace(mode)
    override fun glTexImage2D(
        target: Int,
        level: Int,
        internalformat: Int,
        width: Int,
        height: Int,
        border: Int,
        format: Int,
        type: Int,
        pixels: ByteBuffer?
    ) = GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels)

    override fun glGetFloatv(pname: Int, params: FloatArray) = GL11.glGetFloatv(pname, params)
    override fun glGetError(): Int = GL11.glGetError()
    override fun glClear(mask: Int) = GL11.glClear(mask)
    override fun glClearColor(fl: Float, fl1: Float, fl2: Float, fl3: Float) = GL11.glClearColor(fl, fl1, fl2, fl3)

    override fun glViewport(x: Int, y: Int, w: Int, h: Int) = GL11.glViewport(x, y, w, h)
    override fun glTexParameteri(target: Int, pname: Int, param: Int) = GL11.glTexParameteri(target, pname, param)

    override fun glTexParameterfv(target: Int, pname: Int, param: FloatArray) = GL11.glTexParameterfv(target, pname, param)

}
