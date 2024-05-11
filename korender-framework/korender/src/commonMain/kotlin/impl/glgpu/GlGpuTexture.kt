package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.KorenderException
import com.zakgof.korender.declaration.TextureFilter
import com.zakgof.korender.declaration.TextureWrap
import com.zakgof.korender.impl.gl.VGL11
import com.zakgof.korender.impl.gl.VGL12
import com.zakgof.korender.impl.gl.VGL13
import com.zakgof.korender.impl.gl.VGL14
import com.zakgof.korender.impl.gl.VGL30
import com.zakgof.korender.impl.gl.VGLExt
import com.zakgof.korender.impl.gpu.GpuTexture
import java.nio.ByteBuffer

class GlGpuTexture(private val name: String, val glHandle: Int) : GpuTexture {
    companion object {
        val filterMap = mapOf(
            TextureFilter.Nearest to VGL11.GL_NEAREST,
            TextureFilter.Linear to VGL11.GL_LINEAR,
            TextureFilter.MipMap to VGL11.GL_LINEAR_MIPMAP_LINEAR,
            TextureFilter.MipMapNearestNearest to VGL11.GL_NEAREST_MIPMAP_NEAREST,
            TextureFilter.MipMapLinearNearest to VGL11.GL_LINEAR_MIPMAP_NEAREST,
            TextureFilter.MipMapNearestLinear to VGL11.GL_NEAREST_MIPMAP_LINEAR,
            TextureFilter.MipMapLinearLinear to VGL11.GL_LINEAR_MIPMAP_LINEAR
        )
        val wrapMap = mapOf(
            TextureWrap.MirroredRepeat to VGL14.GL_MIRRORED_REPEAT,
            TextureWrap.ClampToEdge to VGL12.GL_CLAMP_TO_EDGE,
            TextureWrap.Repeat to VGL11.GL_REPEAT
        )
        val formatMap = mapOf(
            GpuTexture.Format.RGBA to VGL11.GL_RGBA,
            GpuTexture.Format.RGB to VGL11.GL_RGB,
        )
    }

    constructor(
        name: String,
        width: Int,
        height: Int,
        bytes: ByteBuffer,
        filter: TextureFilter = TextureFilter.MipMapLinearLinear,
        wrap: TextureWrap = TextureWrap.Repeat,
        aniso: Int = 1024,
        format: GpuTexture.Format = GpuTexture.Format.RGB
    ) : this(name, VGL11.glGenTextures()) {

        println("Creating GPU Texture [$name] $glHandle")

        VGL13.glActiveTexture(VGL13.GL_TEXTURE0)
        VGL11.glBindTexture(VGL11.GL_TEXTURE_2D, glHandle)

        VGL11.glTexImage2D(
            VGL11.GL_TEXTURE_2D,
            0,
            formatMap[format]!!,
            width,
            height,
            0,
            formatMap[format]!!,
            VGL11.GL_UNSIGNED_BYTE,
            bytes
        )

        val errcode = VGL11.glGetError()
        if (errcode != 0) {
            throw KorenderException("Error in glTexImage2D: ${errcode}")
        }

        if (filter !== TextureFilter.Linear && filter !== TextureFilter.Nearest) {
            VGL30.glGenerateMipmap(VGL11.GL_TEXTURE_2D)
        }

        VGL11.glTexParameteri(VGL11.GL_TEXTURE_2D, VGL11.GL_TEXTURE_MIN_FILTER, filterMap[filter]!!)
        VGL11.glTexParameteri(
            VGL11.GL_TEXTURE_2D,
            VGL11.GL_TEXTURE_MAG_FILTER,
            if (filter === TextureFilter.Nearest) VGL11.GL_NEAREST else VGL11.GL_LINEAR
        )

        VGL11.glTexParameteri(VGL11.GL_TEXTURE_2D, VGL11.GL_TEXTURE_WRAP_S, wrapMap[wrap]!!)
        VGL11.glTexParameteri(VGL11.GL_TEXTURE_2D, VGL11.GL_TEXTURE_WRAP_T, wrapMap[wrap]!!)

        if (aniso > 0) {
            val anisoMax = floatArrayOf(0f)
            VGL11.glGetFloatv(VGLExt.GL_MAX_TEXTURE_MAX_ANISOTROPY, anisoMax)
            VGL11.glTexParameteri(
                VGL11.GL_TEXTURE_2D,
                VGLExt.GL_TEXTURE_MAX_ANISOTROPY,
                Math.min(aniso, anisoMax[0].toInt())
            )
        }
        VGL11.glBindTexture(VGL11.GL_TEXTURE_2D, 0)
    }

    override fun close() {
        println("Destroying GPU Texture [$name] $glHandle")
        VGL11.glDeleteTextures(glHandle)
    }

    override fun bind(unit: Int) {
        VGL13.glActiveTexture(VGL13.GL_TEXTURE0 + unit)
        VGL11.glBindTexture(VGL11.GL_TEXTURE_2D, glHandle)
    }

}