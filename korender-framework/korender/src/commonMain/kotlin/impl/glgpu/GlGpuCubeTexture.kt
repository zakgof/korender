package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.CubeTextureImages
import com.zakgof.korender.CubeTextureSide
import com.zakgof.korender.Image
import com.zakgof.korender.KorenderException
import com.zakgof.korender.Platform
import com.zakgof.korender.impl.gl.GL
import com.zakgof.korender.impl.gl.GL.glActiveTexture
import com.zakgof.korender.impl.gl.GL.glBindFramebuffer
import com.zakgof.korender.impl.gl.GL.glBindTexture
import com.zakgof.korender.impl.gl.GL.glCheckFramebufferStatus
import com.zakgof.korender.impl.gl.GL.glDeleteFramebuffers
import com.zakgof.korender.impl.gl.GL.glFramebufferTexture2D
import com.zakgof.korender.impl.gl.GL.glGenFramebuffers
import com.zakgof.korender.impl.gl.GL.glGenTextures
import com.zakgof.korender.impl.gl.GL.glGenerateMipmap
import com.zakgof.korender.impl.gl.GL.glGetError
import com.zakgof.korender.impl.gl.GL.glGetFloatv
import com.zakgof.korender.impl.gl.GL.glReadPixels
import com.zakgof.korender.impl.gl.GL.glTexImage2D
import com.zakgof.korender.impl.gl.GL.glTexParameteri
import com.zakgof.korender.impl.gl.GLConstants
import com.zakgof.korender.impl.gl.GLConstants.GL_CLAMP_TO_EDGE
import com.zakgof.korender.impl.gl.GLConstants.GL_COLOR_ATTACHMENT0
import com.zakgof.korender.impl.gl.GLConstants.GL_FRAMEBUFFER
import com.zakgof.korender.impl.gl.GLConstants.GL_FRAMEBUFFER_COMPLETE
import com.zakgof.korender.impl.gl.GLConstants.GL_MAX_TEXTURE_MAX_ANISOTROPY
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP_NEGATIVE_X
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP_POSITIVE_X
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP_POSITIVE_Y
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP_POSITIVE_Z
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_MAG_FILTER
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_MAX_ANISOTROPY
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_MIN_FILTER
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_WRAP_R
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_WRAP_S
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_WRAP_T
import com.zakgof.korender.impl.gl.GLConstants.GL_UNSIGNED_BYTE
import com.zakgof.korender.impl.gl.GLTexture
import com.zakgof.korender.impl.ignoringGlError
import com.zakgof.korender.impl.image.InternalImage
import kotlin.math.min

internal class GlGpuCubeTexture : GLBindableTexture, AutoCloseable {

    override val glHandle: GLTexture = glGenTextures()

    var width: Int? = null
    var height: Int? = null
    private var format: Image.Format? = null
    private var glFormat: GlGpuTexture.GlFormat? = null

    val sides = listOf(
        GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
        GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
        GL_TEXTURE_CUBE_MAP_NEGATIVE_Z,
        GL_TEXTURE_CUBE_MAP_POSITIVE_X,
        GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
        GL_TEXTURE_CUBE_MAP_POSITIVE_Z
    )

    constructor(images: CubeTextureImages) {

        println("Creating GPU Cube Texture $this")

        glBindTexture(GL_TEXTURE_CUBE_MAP, glHandle)

        width = images[CubeTextureSide.NX]!!.width
        height = images[CubeTextureSide.NX]!!.height
        format = images[CubeTextureSide.NX]!!.format
        glFormat = formatMap[format]!!

        sides.forEachIndexed{ index, glSide ->
            loadSide(glSide, images[CubeTextureSide.entries[index]]!! as InternalImage)
        }

        setupFiltering(GlGpuTexture.Preset.RGBMipmap)
        glGenerateMipmap(GL_TEXTURE_CUBE_MAP)

        glBindTexture(GL_TEXTURE_CUBE_MAP, null)
    }

    constructor(width: Int, height: Int, preset: GlGpuTexture.Preset) {
        println("Creating GPU Cube Texture $this")

        glBindTexture(GL_TEXTURE_CUBE_MAP, glHandle)

        sides.forEach { initSide(it, width, height, preset) }
        setupFiltering(preset)

        glBindTexture(GL_TEXTURE_CUBE_MAP, null)
    }

    private fun setupFiltering(preset: GlGpuTexture.Preset) {

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, filterMinMap[preset.filter]!!)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, filterMagMap[preset.filter]!!)

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE)

        if (preset.aniso > 0) {
            ignoringGlError {
                val anisoMax = glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY) ?: 0f
                if (anisoMax > 0) {
                    glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAX_ANISOTROPY, min(preset.aniso, anisoMax.toInt()))
                }
            }
        }
    }

    private fun loadSide(glSide: Int, image: InternalImage) {
        val glFormat = formatMap[image.format]!!
        glTexImage2D(glSide, 0, glFormat.internal, image.width, image.height, 0, glFormat.format, glFormat.type, image.bytes)
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun initSide(glSide: Int, width: Int, height: Int, preset: GlGpuTexture.Preset) {
        for (glFormat in preset.formats) {
            glTexImage2D(glSide, 0, glFormat.internal, width, height, 0, glFormat.format, glFormat.type, null)
            val errcode = glGetError()
            if (errcode != 0) {
                println("Could not create a cube texture with format 0x${glFormat.internal.toHexString()}. Falling back to next format when creating texture")
                continue
            }
            if (glSide == GL_TEXTURE_CUBE_MAP_NEGATIVE_X) {
                this.width = width
                this.height = height
                this.glFormat = glFormat
                this.format = backFormatMap[glFormat.format]
            }
            return
        }
        throw KorenderException("Could not create GL texture")
    }

    override fun bind(unit: Int) {
        glActiveTexture(GLConstants.GL_TEXTURE0 + unit)
        glBindTexture(GL_TEXTURE_CUBE_MAP, glHandle)
    }

    override fun close() {
        println("Destroying GPU Cube Texture $this")
        GL.glDeleteTextures(glHandle)
    }

    override fun toString() = "$glHandle"

    fun fetch(): CubeTextureImages {
        val fb = glGenFramebuffers()
        glBindFramebuffer(GL_FRAMEBUFFER, fb)

        val images = sides.mapIndexed() { index, side ->
            // Attach cube face to framebuffer
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, side, glHandle, 0)
            val status = glCheckFramebufferStatus(GL_FRAMEBUFFER)
            if (status != GL_FRAMEBUFFER_COMPLETE) {
                throw KorenderException("Framebuffer not complete for face $side: status=$status")
            }
            val img = Platform.createImage(width!!, height!!, format!!)
            glReadPixels(0, 0, width!!, height!!, glFormat!!.format, GL_UNSIGNED_BYTE, img.bytes)
            CubeTextureSide.entries[index] to img
        }.toMap()

        glBindFramebuffer(GL_FRAMEBUFFER, null)
        glDeleteFramebuffers(fb)

        return images
    }
}