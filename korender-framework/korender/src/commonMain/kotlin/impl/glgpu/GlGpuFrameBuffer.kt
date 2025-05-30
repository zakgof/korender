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
import com.zakgof.korender.impl.gl.GLConstants.GL_COLOR_ATTACHMENT0
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_ATTACHMENT
import com.zakgof.korender.impl.gl.GLConstants.GL_FRAMEBUFFER
import com.zakgof.korender.impl.gl.GLConstants.GL_FRAMEBUFFER_COMPLETE
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_2D
import com.zakgof.korender.impl.gl.GLFrameBuffer

internal class GlGpuFrameBuffer(
    private val name: String,
    private val width: Int,
    private val height: Int,
    colorTexturePresets: List<GlGpuTexture.Preset>,
    useDepthBuffer: Boolean
) : AutoCloseable {

    private val fbHandle: GLFrameBuffer = glGenFramebuffers()

    val colorTextures: List<GlGpuTexture>
    val depthTexture: GlGpuTexture?

    init {

        println("Creating GPU Framebuffer $this")

        glBindFramebuffer(GL_FRAMEBUFFER, fbHandle)

        colorTextures = colorTexturePresets.mapIndexed { index, preset ->
            val tex = GlGpuTexture(width, height, preset)
            glFramebufferTexture2D(
                GL_FRAMEBUFFER,
                GL_COLOR_ATTACHMENT0 + index,
                GL_TEXTURE_2D,
                tex.glHandle,
                0
            )
            tex
        }
        println("Framebuffer textures [${colorTextures.map { it.glHandle }}]")

        if (useDepthBuffer) {
            depthTexture = GlGpuTexture(width, height, GlGpuTexture.Preset.Depth)
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
        glDrawBuffers(*IntArray(colorTextures.size) { GL_COLOR_ATTACHMENT0 + it })

        val err: Int = glCheckFramebufferStatus(GL_FRAMEBUFFER)
        if (err != GL_FRAMEBUFFER_COMPLETE) {
            throw KorenderException("Error creating framebuffer $name: $err")
        }

        glBindFramebuffer(GL_FRAMEBUFFER, null)
    }

    override fun close() {
        println("Destroying GPU Framebuffer [$name] $fbHandle ")
        glDeleteFramebuffers(fbHandle)
        colorTextures.forEach { it.close() }
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

    private fun unbind() {
        colorTextures.filter { it.mipmapped }.forEach {
            glBindTexture(GL_TEXTURE_2D, it.glHandle)
            glGenerateMipmap(GL_TEXTURE_2D)
        }
        glBindTexture(GL_TEXTURE_2D, null)
        glBindFramebuffer(GL_FRAMEBUFFER, null)
    }

    override fun toString() = "[$name] ${width}x${height}: $fbHandle"
}