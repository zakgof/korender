package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.KorenderException
import com.zakgof.korender.impl.gl.VGL11
import com.zakgof.korender.impl.gl.VGL13
import com.zakgof.korender.impl.gl.VGL30
import com.zakgof.korender.impl.gpu.GpuFrameBuffer
import java.io.Closeable

class GlGpuFrameBuffer(private val width: Int, private val height: Int, useDepthBuffer: Boolean) :
    GpuFrameBuffer,
    Closeable {

    private val fbHandle: Int = VGL30.glGenFramebuffers()

    override val colorTexture:  GlGpuTexture
    override val depthTexture: GlGpuTexture?

    // TODO: multiple RTs !
    init {

        println("Create Framebuffer ${width}x${height} : $fbHandle")

        VGL30.glBindFramebuffer(VGL30.GL_FRAMEBUFFER, fbHandle)
        colorTexture = createTexture(false)

        VGL30.glFramebufferTexture2D(
            VGL30.GL_FRAMEBUFFER,
            VGL30.GL_COLOR_ATTACHMENT0,
            VGL11.GL_TEXTURE_2D,
            colorTexture.glHandle,
            0
        )
        VGL30.glDrawBuffers(VGL30.GL_COLOR_ATTACHMENT0)
        if (useDepthBuffer) {
            depthTexture = createTexture(true)
            VGL30.glFramebufferTexture2D(
                VGL30.GL_FRAMEBUFFER,
                VGL30.GL_DEPTH_ATTACHMENT,
                VGL11.GL_TEXTURE_2D,
                depthTexture.glHandle,
                0
            )
        } else {
            depthTexture = null
        }
        val err: Int = VGL30.glCheckFramebufferStatus(VGL30.GL_FRAMEBUFFER)
        if (err != VGL30.GL_FRAMEBUFFER_COMPLETE) {
            throw KorenderException("Error creating framebuffer $err")
        }
        VGL30.glBindFramebuffer(VGL30.GL_FRAMEBUFFER, 0)
    }


    private fun createTexture(depth: Boolean): GlGpuTexture {
        val glHandle = VGL11.glGenTextures()
        VGL13.glActiveTexture(VGL13.GL_TEXTURE0)
        VGL11.glBindTexture(VGL11.GL_TEXTURE_2D, glHandle)
        VGL11.glTexImage2D(
            VGL11.GL_TEXTURE_2D,
            0,
            if (depth) VGL11.GL_DEPTH_COMPONENT else VGL11.GL_RGB,
            width,
            height,
            0,
            if (depth) VGL11.GL_DEPTH_COMPONENT else VGL11.GL_RGB,
            if (depth) VGL11.GL_UNSIGNED_SHORT else VGL11.GL_UNSIGNED_BYTE,
            null
        )
        VGL11.glTexParameteri(VGL11.GL_TEXTURE_2D, VGL11.GL_TEXTURE_MIN_FILTER, VGL11.GL_LINEAR)
        VGL11.glTexParameteri(VGL11.GL_TEXTURE_2D, VGL11.GL_TEXTURE_MAG_FILTER, VGL11.GL_LINEAR)
        VGL11.glTexParameteri(VGL11.GL_TEXTURE_2D, VGL11.GL_TEXTURE_WRAP_S, VGL13.GL_CLAMP_TO_BORDER)
        VGL11.glTexParameteri(VGL11.GL_TEXTURE_2D, VGL11.GL_TEXTURE_WRAP_T, VGL13.GL_CLAMP_TO_BORDER)
        VGL11.glTexParameterfv(VGL11.GL_TEXTURE_2D, VGL11.GL_TEXTURE_BORDER_COLOR, floatArrayOf(0f, 0f, 0f, 0f))
        return GlGpuTexture(glHandle)
    }


    override fun close() {
        println("Dispose FrameBuffer $fbHandle ")
        VGL30.glDeleteFramebuffers(fbHandle)
        colorTexture.close()
        depthTexture?.close()
    }

    override fun exec(block: () -> Unit) {
        bind()
        block.invoke()
        unbind()
    }

    private fun bind() {
        VGL30.glBindFramebuffer(VGL30.GL_FRAMEBUFFER, fbHandle)

        val err1 = VGL11.glGetError()
        if (err1 != 0) {
            throw KorenderException("Frame error $err1")
        }

        VGL11.glViewport(0, 0, width, height)
    }

    private fun unbind() {
        VGL30.glBindFramebuffer(VGL30.GL_FRAMEBUFFER, 0)
    }

}