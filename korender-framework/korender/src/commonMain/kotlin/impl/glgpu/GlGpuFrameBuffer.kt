package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.KorenderException
import com.zakgof.korender.impl.gpu.GpuFrameBuffer
import java.io.Closeable

class GlGpuFrameBuffer(private val width: Int, private val height: Int, useDepthBuffer: Boolean) :
    GpuFrameBuffer,
    Closeable {

    private val fbHandle: Int = com.zakgof.korender.impl.gl.VGL30.glGenFramebuffers()

    override val colorTexture:  GlGpuTexture
    override val depthTexture: GlGpuTexture?

    // TODO: multiple RTs !
    init {

        println("Create Framebuffer ${width}x${height} : $fbHandle")

        com.zakgof.korender.impl.gl.VGL30.glBindFramebuffer(com.zakgof.korender.impl.gl.VGL30.GL_FRAMEBUFFER, fbHandle)
        colorTexture = createTexture(false)

        com.zakgof.korender.impl.gl.VGL30.glFramebufferTexture2D(
            com.zakgof.korender.impl.gl.VGL30.GL_FRAMEBUFFER,
            com.zakgof.korender.impl.gl.VGL30.GL_COLOR_ATTACHMENT0,
            com.zakgof.korender.impl.gl.VGL11.GL_TEXTURE_2D,
            colorTexture.glHandle,
            0
        )
        com.zakgof.korender.impl.gl.VGL30.glDrawBuffers(com.zakgof.korender.impl.gl.VGL30.GL_COLOR_ATTACHMENT0)
        if (useDepthBuffer) {
            depthTexture = createTexture(true)
            com.zakgof.korender.impl.gl.VGL30.glFramebufferTexture2D(
                com.zakgof.korender.impl.gl.VGL30.GL_FRAMEBUFFER,
                com.zakgof.korender.impl.gl.VGL30.GL_DEPTH_ATTACHMENT,
                com.zakgof.korender.impl.gl.VGL11.GL_TEXTURE_2D,
                depthTexture.glHandle,
                0
            )
        } else {
            depthTexture = null
        }
        val err: Int = com.zakgof.korender.impl.gl.VGL30.glCheckFramebufferStatus(com.zakgof.korender.impl.gl.VGL30.GL_FRAMEBUFFER)
        if (err != com.zakgof.korender.impl.gl.VGL30.GL_FRAMEBUFFER_COMPLETE) {
            throw KorenderException("Error creating framebuffer ${err}")
        }
        com.zakgof.korender.impl.gl.VGL30.glBindFramebuffer(com.zakgof.korender.impl.gl.VGL30.GL_FRAMEBUFFER, 0)
    }


    private fun createTexture(depth: Boolean): GlGpuTexture {
        val glHandle = com.zakgof.korender.impl.gl.VGL11.glGenTextures()
        com.zakgof.korender.impl.gl.VGL13.glActiveTexture(com.zakgof.korender.impl.gl.VGL13.GL_TEXTURE0)
        com.zakgof.korender.impl.gl.VGL11.glBindTexture(com.zakgof.korender.impl.gl.VGL11.GL_TEXTURE_2D, glHandle)
        com.zakgof.korender.impl.gl.VGL11.glTexImage2D(
            com.zakgof.korender.impl.gl.VGL11.GL_TEXTURE_2D,
            0,
            if (depth) com.zakgof.korender.impl.gl.VGL11.GL_DEPTH_COMPONENT else com.zakgof.korender.impl.gl.VGL11.GL_RGB,
            width,
            height,
            0,
            if (depth) com.zakgof.korender.impl.gl.VGL11.GL_DEPTH_COMPONENT else com.zakgof.korender.impl.gl.VGL11.GL_RGB,
            com.zakgof.korender.impl.gl.VGL11.GL_UNSIGNED_BYTE,
            null
        )
        com.zakgof.korender.impl.gl.VGL11.glTexParameteri(com.zakgof.korender.impl.gl.VGL11.GL_TEXTURE_2D, com.zakgof.korender.impl.gl.VGL11.GL_TEXTURE_MIN_FILTER, com.zakgof.korender.impl.gl.VGL11.GL_LINEAR);
        com.zakgof.korender.impl.gl.VGL11.glTexParameteri(com.zakgof.korender.impl.gl.VGL11.GL_TEXTURE_2D, com.zakgof.korender.impl.gl.VGL11.GL_TEXTURE_MAG_FILTER, com.zakgof.korender.impl.gl.VGL11.GL_LINEAR);
        com.zakgof.korender.impl.gl.VGL11.glTexParameteri(com.zakgof.korender.impl.gl.VGL11.GL_TEXTURE_2D, com.zakgof.korender.impl.gl.VGL11.GL_TEXTURE_WRAP_S, com.zakgof.korender.impl.gl.VGL12.GL_CLAMP_TO_EDGE)
        com.zakgof.korender.impl.gl.VGL11.glTexParameteri(com.zakgof.korender.impl.gl.VGL11.GL_TEXTURE_2D, com.zakgof.korender.impl.gl.VGL11.GL_TEXTURE_WRAP_T, com.zakgof.korender.impl.gl.VGL12.GL_CLAMP_TO_EDGE)
        return GlGpuTexture(glHandle)
    }


    override fun close() {
        println("Dispose FrameBuffer $fbHandle ")
        com.zakgof.korender.impl.gl.VGL30.glDeleteFramebuffers(fbHandle)
        colorTexture.close()
        depthTexture?.close()
    }

    override fun exec(block: () -> Unit) {
        bind()
        block.invoke()
        unbind()
    }

    private fun bind() {
        com.zakgof.korender.impl.gl.VGL30.glBindFramebuffer(com.zakgof.korender.impl.gl.VGL30.GL_FRAMEBUFFER, fbHandle)

        val err1 = com.zakgof.korender.impl.gl.VGL11.glGetError()
        if (err1 != 0) {
            throw KorenderException("Frame error $err1")
        }

        com.zakgof.korender.impl.gl.VGL11.glViewport(0, 0, width, height)
    }

    private fun unbind() {
        com.zakgof.korender.impl.gl.VGL30.glBindFramebuffer(com.zakgof.korender.impl.gl.VGL30.GL_FRAMEBUFFER, 0)
    }

}