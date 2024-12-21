package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.KorenderException
import com.zakgof.korender.buffer.Byter
import com.zakgof.korender.gl.GL.glActiveTexture
import com.zakgof.korender.gl.GL.glBindTexture
import com.zakgof.korender.gl.GL.glDeleteTextures
import com.zakgof.korender.gl.GL.glGenTextures
import com.zakgof.korender.gl.GL.glGenerateMipmap
import com.zakgof.korender.gl.GL.glGetError
import com.zakgof.korender.gl.GL.glTexImage2D
import com.zakgof.korender.gl.GL.glTexParameteri
import com.zakgof.korender.gl.GLConstants.GL_CLAMP_TO_EDGE
import com.zakgof.korender.gl.GLConstants.GL_LINEAR
import com.zakgof.korender.gl.GLConstants.GL_LINEAR_MIPMAP_LINEAR
import com.zakgof.korender.gl.GLConstants.GL_LINEAR_MIPMAP_NEAREST
import com.zakgof.korender.gl.GLConstants.GL_MIRRORED_REPEAT
import com.zakgof.korender.gl.GLConstants.GL_NEAREST
import com.zakgof.korender.gl.GLConstants.GL_NEAREST_MIPMAP_LINEAR
import com.zakgof.korender.gl.GLConstants.GL_NEAREST_MIPMAP_NEAREST
import com.zakgof.korender.gl.GLConstants.GL_REPEAT
import com.zakgof.korender.gl.GLConstants.GL_RGB
import com.zakgof.korender.gl.GLConstants.GL_RGBA
import com.zakgof.korender.gl.GLConstants.GL_TEXTURE0
import com.zakgof.korender.gl.GLConstants.GL_TEXTURE_2D
import com.zakgof.korender.gl.GLConstants.GL_TEXTURE_MAG_FILTER
import com.zakgof.korender.gl.GLConstants.GL_TEXTURE_MIN_FILTER
import com.zakgof.korender.gl.GLConstants.GL_TEXTURE_WRAP_S
import com.zakgof.korender.gl.GLConstants.GL_TEXTURE_WRAP_T
import com.zakgof.korender.gl.GLConstants.GL_UNSIGNED_BYTE
import com.zakgof.korender.gl.GLTexture
import com.zakgof.korender.impl.gpu.GpuTexture
import com.zakgof.korender.material.TextureFilter
import com.zakgof.korender.material.TextureWrap

class GlGpuTexture(private val name: String, val glHandle: GLTexture) : GpuTexture {
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
            GpuTexture.Format.RGBA to GL_RGBA,
            GpuTexture.Format.RGB to GL_RGB,
        )
    }

    constructor(
        name: String,
        width: Int,
        height: Int,
        bytes: Byter,
        filter: TextureFilter = TextureFilter.MipMapLinearLinear,
        wrap: TextureWrap = TextureWrap.Repeat,
        aniso: Int = 1024,
        format: GpuTexture.Format = GpuTexture.Format.RGB
    ) : this(name, glGenTextures()) {

        println("Creating GPU Texture [$name] $glHandle")

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, glHandle)

        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            formatMap[format]!!,
            width,
            height,
            0,
            formatMap[format]!!,
            GL_UNSIGNED_BYTE,
            bytes
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

        /*
        TODO check if extension is available ? error 1280

        if (aniso > 0) {
            val anisoMax = glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY) ?: 0f
            if (anisoMax > 0) {
                glTexParameteri(
                    GL_TEXTURE_2D,
                    GL_TEXTURE_MAX_ANISOTROPY,
                    min(aniso, anisoMax.toInt())
                )
            }
        }
        */

        // TODO attention
        // glBindTexture(GL_TEXTURE_2D, 0)
    }

    override fun close() {
        println("Destroying GPU Texture [$name] $glHandle")
        glDeleteTextures(glHandle)
    }

    override fun bind(unit: Int) {
        glActiveTexture(GL_TEXTURE0 + unit)
        glBindTexture(GL_TEXTURE_2D, glHandle)
    }

}