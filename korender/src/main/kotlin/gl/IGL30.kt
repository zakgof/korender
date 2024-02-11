package gl

interface IGL30 {
    fun glGenerateMipmap(target: Int)

    fun glGenFramebuffers(): Int

    fun glFramebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int)

    fun glGenRenderbuffers(): Int

    fun glRenderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int)

    fun glFramebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: Int)

    fun glDrawBuffers(buf: Int)

    fun glDeleteFramebuffers(framebuffer: Int)

    fun glDeleteRenderbuffers(renderbuffer: Int)

    fun glBindFramebuffer(target: Int, framebuffer: Int)

    fun glBindRenderbuffer(target: Int, renderbuffer: Int)

    fun glCheckFramebufferStatus(target: Int): Int
}
