package com.zakgof.korender.impl.glgpu

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
import com.zakgof.korender.impl.gl.GLConstants.GL_R16
import com.zakgof.korender.impl.gl.GLConstants.GL_R8
import com.zakgof.korender.impl.gl.GLConstants.GL_RED
import com.zakgof.korender.impl.gl.GLConstants.GL_RGB
import com.zakgof.korender.impl.gl.GLConstants.GL_RGBA
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
import com.zakgof.korender.impl.gl.GLConstants.GL_UNSIGNED_SHORT
import com.zakgof.korender.impl.gl.GLTexture
import com.zakgof.korender.impl.ignoringGlError
import com.zakgof.korender.impl.image.InternalImage
import kotlin.math.min

internal class GlGpuCubeTexture : AutoCloseable {

    val glHandle: GLTexture = glGenTextures()

    private var width: Int? = null
    private var height: Int? = null
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

    private val formatMap = mapOf(
        Image.Format.RGBA to GlGpuTexture.GlFormat(GL_RGBA, GL_RGBA, GL_UNSIGNED_BYTE),
        Image.Format.RGB to GlGpuTexture.GlFormat(GL_RGB, GL_RGB, GL_UNSIGNED_BYTE),
        Image.Format.Gray to GlGpuTexture.GlFormat(GL_R8, GL_RED, GL_UNSIGNED_BYTE),
        Image.Format.Gray16 to GlGpuTexture.GlFormat(GL_R16, GL_RED, GL_UNSIGNED_SHORT)
    )

    private val backFormatMap = mapOf(
        GL_RGBA to Image.Format.RGBA,
        GL_RGB to Image.Format.RGB,
        GL_R8 to Image.Format.Gray,
        GL_R16 to Image.Format.Gray16
    )

    constructor(
        imageNx: InternalImage,
        imageNy: InternalImage,
        imageNz: InternalImage,
        imagePx: InternalImage,
        imagePy: InternalImage,
        imagePz: InternalImage,
    ) {

        println("Creating GPU Cube Texture $this")

        glBindTexture(GL_TEXTURE_CUBE_MAP, glHandle)

        width = imageNx.width
        height = imageNx.height
        format = imageNx.format
        glFormat = formatMap[format]!!

        loadSide(GL_TEXTURE_CUBE_MAP_NEGATIVE_X, imageNx)
        loadSide(GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, imageNy)
        loadSide(GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, imageNz)
        loadSide(GL_TEXTURE_CUBE_MAP_POSITIVE_X, imagePx)
        loadSide(GL_TEXTURE_CUBE_MAP_POSITIVE_Y, imagePy)
        loadSide(GL_TEXTURE_CUBE_MAP_POSITIVE_Z, imagePz)

        setupFiltering(GlGpuTexture.Preset.RGBFilter)
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

    fun bind(unit: Int) {
        glActiveTexture(GLConstants.GL_TEXTURE0 + unit)
        glBindTexture(GL_TEXTURE_CUBE_MAP, glHandle)
    }

    override fun close() {
        println("Destroying GPU Cube Texture $this")
        GL.glDeleteTextures(glHandle)
    }

    override fun toString() = "$glHandle"

    fun fetch(): List<Image> {
        val fb = glGenFramebuffers()
        glBindFramebuffer(GL_FRAMEBUFFER, fb)

        val images = sides.map {
            // Attach cube face to framebuffer
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, it, glHandle, 0)
            val status = glCheckFramebufferStatus(GL_FRAMEBUFFER)
            if (status != GL_FRAMEBUFFER_COMPLETE) {
                throw KorenderException("Framebuffer not complete for face $it: status=$status")
            }
            val img = Platform.createImage(width!!, height!!, format!!)
            glReadPixels(0, 0, width!!, height!!, glFormat!!.format, GL_UNSIGNED_BYTE, img.bytes)
            img
        }

        glBindFramebuffer(GL_FRAMEBUFFER, null)
        glDeleteFramebuffers(fb)

        return images
    }
}