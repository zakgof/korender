package com.zakgof.korender.lwjgl

import gl.IGL30
import org.lwjgl.opengl.GL30

class Lwjgl30 : IGL30 {
    override fun glGenerateMipmap(target: Int) {
        GL30.glGenerateMipmap(target)
    }

    override fun glGenFramebuffers(): Int {
        return GL30.glGenFramebuffers()
    }

    override fun glFramebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int) {
        GL30.glFramebufferTexture2D(target, attachment, textarget, texture, level)
    }

    override fun glGenRenderbuffers(): Int {
        return GL30.glGenRenderbuffers()
    }

    override fun glRenderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int) {
        GL30.glRenderbufferStorage(target, internalformat, width, height)
    }

    override fun glFramebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: Int) {
        GL30.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer)
    }

    override fun glDrawBuffers(buf: Int) {
        GL30.glDrawBuffer(buf)
    }

    override fun glDeleteFramebuffers(framebuffer: Int) {
        GL30.glDeleteFramebuffers(framebuffer)
    }

    override fun glDeleteRenderbuffers(renderbuffer: Int) {
        GL30.glDeleteRenderbuffers(renderbuffer)
    }

    override fun glBindFramebuffer(target: Int, framebuffer: Int) {
        GL30.glBindFramebuffer(target, framebuffer)
    }

    override fun glBindRenderbuffer(target: Int, renderbuffer: Int) {
        GL30.glBindRenderbuffer(target, renderbuffer)
    }

    override fun glCheckFramebufferStatus(target: Int): Int {
        return GL30.glCheckFramebufferStatus(target)
    }
}
