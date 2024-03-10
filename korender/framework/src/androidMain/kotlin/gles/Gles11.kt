package com.zakgof.korender.gles

import android.opengl.GLES11
import com.zakgof.korender.gl.IGL11
import java.nio.ByteBuffer

object Gles11 : IGL11 {

    override val shaderEnv = "GLES"

    override fun glDrawElements(mode: Int, count: Int, type: Int, indices: Int) =
        GLES11.glDrawElements(mode, count, type, indices)

    override fun glEnable(target: Int) = GLES11.glEnable(target)
    override fun glBindTexture(target: Int, texture: Int) = GLES11.glBindTexture(target, texture)
    override fun glTexParameterf(target: Int, pname: Int, param: Float) =
        GLES11.glTexParameterf(target, pname, param)

    override fun glDeleteTextures(texture: Int) = GLES11.glDeleteTextures(1, intArrayOf(texture), 0)
    override fun glPixelStorei(pname: Int, param: Int) = GLES11.glPixelStorei(pname, param)
    override fun glGenTextures(): Int {
        val array = IntArray(1)
        GLES11.glGenTextures(1, array, 0)
        return array[0]
    }
    override fun glBlendFunc(sfactor: Int, dfactor: Int) = GLES11.glBlendFunc(sfactor, dfactor)
    override fun glDepthMask(flag: Boolean) = GLES11.glDepthMask(flag)
    override fun glCullFace(mode: Int) = GLES11.glCullFace(mode)
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
    ) = GLES11.glTexImage2D(
        target,
        level,
        internalformat,
        width,
        height,
        border,
        format,
        type,
        pixels
    )

    override fun glGetFloatv(pname: Int, params: FloatArray) = GLES11.glGetFloatv(pname, params, 0)
    override fun glGetError(): Int = GLES11.glGetError()
    override fun glClear(mask: Int) = GLES11.glClear(mask)
    override fun glClearColor(fl: Float, fl1: Float, fl2: Float, fl3: Float) = GLES11.glClearColor(fl, fl1, fl2, fl3)
    override fun glViewport(x: Int, y: Int, w: Int, h: Int) = GLES11.glViewport(x, y, w, h)
    override fun glTexParameteri(target: Int, pname: Int, param: Int) =
        GLES11.glTexParameteri(target, pname, param)

}
