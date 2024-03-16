package com.zakgof.korender.gl

import java.nio.ByteBuffer

interface IGL11 {

    val shaderEnv: String

    fun glDrawElements(mode: Int, count: Int, type: Int, indices: Int)

    fun glEnable(target: Int)

    fun glBindTexture(target: Int, texture: Int)

    fun glTexParameterf(target: Int, pname: Int, param: Float)

    fun glDeleteTextures(texture: Int)

    fun glPixelStorei(pname: Int, param: Int)

    fun glGenTextures(): Int

    fun glBlendFunc(sfactor: Int, dfactor: Int)

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
        pixels: ByteBuffer?
    )

    fun glGetFloatv(pname: Int, params: FloatArray)

    fun glGetError(): Int

    fun glClear(mask: Int)

    fun glViewport(x: Int, y: Int, w: Int, h: Int)

    fun glTexParameteri(target: Int, pname: Int, param: Int)

    fun glClearColor(fl: Float, fl1: Float, fl2: Float, fl3: Float)
}
