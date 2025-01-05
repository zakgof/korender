package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.KorenderException
import com.zakgof.korender.impl.gl.GL.glActiveTexture
import com.zakgof.korender.impl.gl.GL.glBindFramebuffer
import com.zakgof.korender.impl.gl.GL.glBindTexture
import com.zakgof.korender.impl.gl.GL.glCheckFramebufferStatus
import com.zakgof.korender.impl.gl.GL.glDeleteFramebuffers
import com.zakgof.korender.impl.gl.GL.glFramebufferTexture2D
import com.zakgof.korender.impl.gl.GL.glGenFramebuffers
import com.zakgof.korender.impl.gl.GL.glGenTextures
import com.zakgof.korender.impl.gl.GL.glTexImage2D
import com.zakgof.korender.impl.gl.GL.glTexParameterfv
import com.zakgof.korender.impl.gl.GL.glTexParameteri
import com.zakgof.korender.impl.gl.GL.glViewport
import com.zakgof.korender.impl.gl.GL.shaderEnv
import com.zakgof.korender.impl.gl.GLConstants.GL_CLAMP_TO_BORDER
import com.zakgof.korender.impl.gl.GLConstants.GL_COLOR_ATTACHMENT0
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_ATTACHMENT
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_COMPONENT
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_COMPONENT16
import com.zakgof.korender.impl.gl.GLConstants.GL_FRAMEBUFFER
import com.zakgof.korender.impl.gl.GLConstants.GL_FRAMEBUFFER_COMPLETE
import com.zakgof.korender.impl.gl.GLConstants.GL_LINEAR
import com.zakgof.korender.impl.gl.GLConstants.GL_NEAREST
import com.zakgof.korender.impl.gl.GLConstants.GL_RGB
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE0
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_2D
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_BORDER_COLOR
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_MAG_FILTER
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_MIN_FILTER
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_WRAP_S
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_WRAP_T
import com.zakgof.korender.impl.gl.GLConstants.GL_UNSIGNED_BYTE
import com.zakgof.korender.impl.gl.GLConstants.GL_UNSIGNED_SHORT
import com.zakgof.korender.impl.gl.GLFrameBuffer
import com.zakgof.korender.impl.ignoringGlError

internal class GlGpuFrameBuffer(private val name: String, private val width: Int, private val height: Int, useDepthBuffer: Boolean) :
    AutoCloseable {

    private val fbHandle: GLFrameBuffer = glGenFramebuffers()

    val colorTexture:  GlGpuTexture
    val depthTexture: GlGpuTexture?

    init {

        println("Creating GPU Framebuffer [$name] ${width}x${height}: $fbHandle")

        glBindFramebuffer(GL_FRAMEBUFFER, fbHandle)
        colorTexture = createTexture(false)
        glFramebufferTexture2D(
            GL_FRAMEBUFFER,
            GL_COLOR_ATTACHMENT0,
            GL_TEXTURE_2D,
            colorTexture.glHandle,
            0
        )

        if (useDepthBuffer) {
            depthTexture = createTexture(true)
            glFramebufferTexture2D(
                GL_FRAMEBUFFER,
                GL_DEPTH_ATTACHMENT,
                GL_TEXTURE_2D,
                depthTexture.glHandle,
                0
            )
        } else {
            depthTexture = null
        }
        val err: Int = glCheckFramebufferStatus(GL_FRAMEBUFFER)
        if (err != GL_FRAMEBUFFER_COMPLETE) {
            throw KorenderException("Error creating framebuffer $err")
        }
    }


    private fun createTexture(depth: Boolean): GlGpuTexture {
        val glHandle = glGenTextures()
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, glHandle)
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            if (depth) (if (shaderEnv == "WEBGL") GL_DEPTH_COMPONENT16 else GL_DEPTH_COMPONENT) else GL_RGB,
            width,
            height,
            0,
            if (depth) GL_DEPTH_COMPONENT else GL_RGB,
            if (depth) GL_UNSIGNED_SHORT else GL_UNSIGNED_BYTE,
            null
        )

        if (depth && shaderEnv == "WEBGL") {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        } else {
            ignoringGlError {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER)
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER)
                glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, floatArrayOf(0f, 0f, 0f, 0f))
            }
        }
        return GlGpuTexture("$name-${if (depth) "depth" else "tex"}", glHandle)
    }

    override fun close() {
        println("Destroying GPU Framebuffer [$name] $fbHandle ")
        glDeleteFramebuffers(fbHandle)
        colorTexture.close()
        depthTexture?.close()
    }

    fun exec(block: () -> Unit) {
        bind()
        block.invoke()
        unbind()
    }

    private fun bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, fbHandle)
        glViewport(0, 0, width, height)
    }

    private fun unbind() = glBindFramebuffer(GL_FRAMEBUFFER, null)
}