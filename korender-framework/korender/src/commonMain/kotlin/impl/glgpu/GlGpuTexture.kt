package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.Image
import com.zakgof.korender.KorenderException
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.impl.gl.GL.glActiveTexture
import com.zakgof.korender.impl.gl.GL.glBindTexture
import com.zakgof.korender.impl.gl.GL.glDeleteTextures
import com.zakgof.korender.impl.gl.GL.glGenTextures
import com.zakgof.korender.impl.gl.GL.glGenerateMipmap
import com.zakgof.korender.impl.gl.GL.glGetError
import com.zakgof.korender.impl.gl.GL.glGetFloatv
import com.zakgof.korender.impl.gl.GL.glTexImage2D
import com.zakgof.korender.impl.gl.GL.glTexParameteri
import com.zakgof.korender.impl.gl.GLConstants.GL_CLAMP_TO_EDGE
import com.zakgof.korender.impl.gl.GLConstants.GL_LINEAR
import com.zakgof.korender.impl.gl.GLConstants.GL_LINEAR_MIPMAP_LINEAR
import com.zakgof.korender.impl.gl.GLConstants.GL_LINEAR_MIPMAP_NEAREST
import com.zakgof.korender.impl.gl.GLConstants.GL_MAX_TEXTURE_MAX_ANISOTROPY
import com.zakgof.korender.impl.gl.GLConstants.GL_MIRRORED_REPEAT
import com.zakgof.korender.impl.gl.GLConstants.GL_NEAREST
import com.zakgof.korender.impl.gl.GLConstants.GL_NEAREST_MIPMAP_LINEAR
import com.zakgof.korender.impl.gl.GLConstants.GL_NEAREST_MIPMAP_NEAREST
import com.zakgof.korender.impl.gl.GLConstants.GL_REPEAT
import com.zakgof.korender.impl.gl.GLConstants.GL_RGB
import com.zakgof.korender.impl.gl.GLConstants.GL_RGBA
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE0
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_2D
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_MAG_FILTER
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_MAX_ANISOTROPY
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_MIN_FILTER
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_WRAP_S
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_WRAP_T
import com.zakgof.korender.impl.gl.GLConstants.GL_UNSIGNED_BYTE
import com.zakgof.korender.impl.gl.GLTexture
import com.zakgof.korender.impl.ignoringGlError
import com.zakgof.korender.impl.image.InternalImage
import kotlin.math.min

internal class GlGpuTexture(private val name: String, val glHandle: GLTexture) : AutoCloseable {

    companion object {
        val filterMap = mapOf(
            TextureFilter.Nearest to GL_NEAREST,
            TextureFilter.Linear to GL_LINEAR,
            TextureFilter.MipMap to GL_LINEAR_MIPMAP_LINEAR,
            TextureFilter.MipMapNearestNearest to GL_NEAREST_MIPMAP_NEAREST,
            TextureFilter.MipMapLinearNearest to GL_LINEAR_MIPMAP_NEAREST,
            TextureFilter.MipMapNearestLinear to GL_NEAREST_MIPMAP_LINEAR,
            TextureFilter.MipMapLinearLinear to GL_LINEAR_MIPMAP_LINEAR
        )
        val wrapMap = mapOf(
            TextureWrap.MirroredRepeat to GL_MIRRORED_REPEAT,
            TextureWrap.ClampToEdge to GL_CLAMP_TO_EDGE,
            TextureWrap.Repeat to GL_REPEAT
        )
        val formatMap = mapOf(
            Image.Format.RGBA to GL_RGBA,
            Image.Format.RGB to GL_RGB,
        )
    }

    constructor(
        name: String,
        image: InternalImage,
        filter: TextureFilter = TextureFilter.MipMapLinearLinear,
        wrap: TextureWrap = TextureWrap.Repeat,
        aniso: Int = 1024,
    ) : this(name, glGenTextures()) {

        println("Creating GPU Texture $this")

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, glHandle)

        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            formatMap[image.format]!!,
            image.width,
            image.height,
            0,
            formatMap[image.format]!!,
            GL_UNSIGNED_BYTE,
            image.bytes
        )

        val errcode = glGetError()
        if (errcode != 0) {
            throw KorenderException("Error in glTexImage2D: ${errcode}")
        }

        if (filter !== TextureFilter.Linear && filter !== TextureFilter.Nearest) {
            glGenerateMipmap(GL_TEXTURE_2D)
        }

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filterMap[filter]!!)
        glTexParameteri(
            GL_TEXTURE_2D,
            GL_TEXTURE_MAG_FILTER,
            if (filter === TextureFilter.Nearest) GL_NEAREST else GL_LINEAR
        )

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapMap[wrap]!!)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapMap[wrap]!!)

        if (aniso > 0) {
            ignoringGlError {
                val anisoMax = glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY) ?: 0f
                if (anisoMax > 0) {
                    glTexParameteri(
                        GL_TEXTURE_2D,
                        GL_TEXTURE_MAX_ANISOTROPY,
                        min(aniso, anisoMax.toInt())
                    )
                }
            }
        }
    }

    fun bind(unit: Int) {
        glActiveTexture(GL_TEXTURE0 + unit)
        glBindTexture(GL_TEXTURE_2D, glHandle)
    }

    override fun close() {
        println("Destroying GPU Texture $this")
        glDeleteTextures(glHandle)
    }

    override fun toString() = "[$name] $glHandle"

}