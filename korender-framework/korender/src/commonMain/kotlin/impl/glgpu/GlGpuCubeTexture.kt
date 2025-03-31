package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.Image
import com.zakgof.korender.KorenderException
import com.zakgof.korender.impl.gl.GL
import com.zakgof.korender.impl.gl.GL.glActiveTexture
import com.zakgof.korender.impl.gl.GL.glBindTexture
import com.zakgof.korender.impl.gl.GL.glGenTextures
import com.zakgof.korender.impl.gl.GL.glGenerateMipmap
import com.zakgof.korender.impl.gl.GL.glGetError
import com.zakgof.korender.impl.gl.GL.glGetFloatv
import com.zakgof.korender.impl.gl.GL.glTexImage2D
import com.zakgof.korender.impl.gl.GL.glTexParameteri
import com.zakgof.korender.impl.gl.GLConstants
import com.zakgof.korender.impl.gl.GLConstants.GL_CLAMP_TO_EDGE
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
}