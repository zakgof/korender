package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.KorenderException
import com.zakgof.korender.impl.gl.GL.glBindFramebuffer
import com.zakgof.korender.impl.gl.GL.glBindTexture
import com.zakgof.korender.impl.gl.GL.glCheckFramebufferStatus
import com.zakgof.korender.impl.gl.GL.glDeleteFramebuffers
import com.zakgof.korender.impl.gl.GL.glDrawBuffers
import com.zakgof.korender.impl.gl.GL.glFramebufferTexture2D
import com.zakgof.korender.impl.gl.GL.glGenFramebuffers
import com.zakgof.korender.impl.gl.GL.glGenerateMipmap
import com.zakgof.korender.impl.gl.GL.glViewport
import com.zakgof.korender.impl.gl.GLConstant
import com.zakgof.korender.impl.gl.GLConstants.GL_COLOR_ATTACHMENT0
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_ATTACHMENT
import com.zakgof.korender.impl.gl.GLConstants.GL_FRAMEBUFFER
import com.zakgof.korender.impl.gl.GLConstants.GL_FRAMEBUFFER_COMPLETE
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP
import com.zakgof.korender.impl.gl.GLFrameBuffer

internal class GlGpuCubeFrameBuffer(
    private val name: String,
    private val width: Int,
    private val height: Int,
    useDepthBuffer: Boolean
) : AutoCloseable {

    private val fbHandle: GLFrameBuffer = glGenFramebuffers()

    val colorTexture: GlGpuCubeTexture
    val depthTexture: GlGpuCubeTexture?

    init {
        println("Creating GPU Cube Framebuffer $this")
        glBindFramebuffer(GL_FRAMEBUFFER, fbHandle)
        colorTexture = GlGpuCubeTexture(width, height, GlGpuTexture.Preset.RGBAFilter)
        colorTexture.sides.forEach {
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, it, colorTexture.glHandle, 0)
        }

        depthTexture = if (useDepthBuffer) {
            GlGpuCubeTexture(width, height, GlGpuTexture.Preset.Depth).apply {
                sides.forEach {
                    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, it, glHandle,0)
                }
            }
        } else
            null

        glDrawBuffers(GL_COLOR_ATTACHMENT0)
        val err: Int = glCheckFramebufferStatus(GL_FRAMEBUFFER)
        if (err != GL_FRAMEBUFFER_COMPLETE) {
            throw KorenderException("Error creating cube framebuffer $name: $err")
        }
        glBindFramebuffer(GL_FRAMEBUFFER, null)
    }

    override fun close() {
        println("Destroying GPU Cube Framebuffer [$name] $fbHandle")
        glDeleteFramebuffers(fbHandle)
        colorTexture.close()
        depthTexture?.close()
    }

    fun exec(glSide: GLConstant, block: () -> Unit) {
        bind(glSide)
        block.invoke()
    }

    private fun bind(glSide: GLConstant) {
        glBindFramebuffer(GL_FRAMEBUFFER, fbHandle)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, glSide, colorTexture.glHandle, 0)
        if (depthTexture != null) {
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, glSide, depthTexture.glHandle, 0)
        }
        glViewport(0, 0, width, height)
    }

    fun finish() {
        glBindTexture(GL_TEXTURE_CUBE_MAP, colorTexture.glHandle)
        glGenerateMipmap(GL_TEXTURE_CUBE_MAP)
        glBindTexture(GL_TEXTURE_CUBE_MAP, null)
        glBindFramebuffer(GL_FRAMEBUFFER, null)
    }

    override fun toString() = "[$name] ${width}x${height}: $fbHandle"
}