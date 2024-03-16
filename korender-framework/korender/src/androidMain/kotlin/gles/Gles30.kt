package com.zakgof.korender.gles

import android.opengl.GLES30
import com.zakgof.korender.gl.IGL30

object Gles30 : IGL30 {

    override fun glGenerateMipmap(target: Int) = GLES30.glGenerateMipmap(target)

    override fun glGenFramebuffers(): Int {
        val array = IntArray(1)
        GLES30.glGenFramebuffers(1, array, 0)
        return array[0]
    }

    override fun glFramebufferTexture2D(
        target: Int,
        attachment: Int,
        textarget: Int,
        texture: Int,
        level: Int
    ) =
        GLES30.glFramebufferTexture2D(target, attachment, textarget, texture, level)

    override fun glGenRenderbuffers(): Int {
        val array = IntArray(1)
        GLES30.glGenRenderbuffers(1, array, 0)
        return array[0]
    }

    override fun glRenderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int) =
        GLES30.glRenderbufferStorage(target, internalformat, width, height)

    override fun glFramebufferRenderbuffer(
        target: Int,
        attachment: Int,
        renderbuffertarget: Int,
        renderbuffer: Int
    ) =
        GLES30.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer)

    override fun glDrawBuffers(buf: Int) =
        GLES30.glDrawBuffers(1, intArrayOf(buf), 0)

    override fun glDeleteFramebuffers(framebuffer: Int) =
        GLES30.glDeleteFramebuffers(1, intArrayOf(framebuffer), 0)

    override fun glDeleteRenderbuffers(renderbuffer: Int) =
        GLES30.glDeleteRenderbuffers(1, intArrayOf(renderbuffer), 0)

    override fun glBindFramebuffer(target: Int, framebuffer: Int) =
        GLES30.glBindFramebuffer(target, framebuffer)

    override fun glBindRenderbuffer(target: Int, renderbuffer: Int) =
        GLES30.glBindRenderbuffer(target, renderbuffer)

    override fun glCheckFramebufferStatus(target: Int): Int =
        GLES30.glCheckFramebufferStatus(target)
}
