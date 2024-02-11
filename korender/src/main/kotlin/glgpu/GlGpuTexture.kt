package com.zakgof.korender.glgpu

import com.zakgof.korender.KorenderException
import com.zakgof.korender.material.TextureFilter
import com.zakgof.korender.material.TextureWrap
import com.zakgof.korender.gpu.GpuTexture
import gl.*
import java.nio.ByteBuffer

class GlGpuTexture(
    width: Int,
    height: Int,
    bytes: ByteBuffer,
    filter: TextureFilter = TextureFilter.MipMapLinearLinear,
    wrap: TextureWrap = TextureWrap.Repeat,
    aniso: Int = 0
) : GpuTexture {

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
    }

    val glHandle: Int

    init {
        glHandle = VGL11.glGenTextures()

        VGL13.glActiveTexture(VGL13.GL_TEXTURE0)
        VGL11.glBindTexture(VGL11.GL_TEXTURE_2D, glHandle)

        VGL11.glTexImage2D(
            VGL11.GL_TEXTURE_2D,
            0,
            VGL11.GL_RGB,
            width,
            height,
            0,
            VGL11.GL_RGB,
            VGL11.GL_UNSIGNED_BYTE,
            bytes
        )
        if (VGL11.glGetError() != 0) {
            throw KorenderException("Error in glTexImage2D")
        }

        if (filter !== TextureFilter.Linear && filter !== TextureFilter.Nearest) {
            VGL30.glGenerateMipmap(VGL11.GL_TEXTURE_2D)
        }

        VGL11.glTexParameteri(VGL11.GL_TEXTURE_2D, VGL11.GL_TEXTURE_MIN_FILTER, filterMap[filter]!!)
        VGL11.glTexParameteri(
            VGL11.GL_TEXTURE_2D,
            VGL11.GL_TEXTURE_MAG_FILTER,
            filterMap[if (filter === TextureFilter.Linear) TextureFilter.Nearest else filter]!!
        )

        VGL11.glTexParameteri(VGL11.GL_TEXTURE_2D, VGL11.GL_TEXTURE_WRAP_S, wrapMap[wrap]!!)
        VGL11.glTexParameteri(VGL11.GL_TEXTURE_2D, VGL11.GL_TEXTURE_WRAP_T, wrapMap[wrap]!!)

        if (aniso > 0) {
            val anisoMax = floatArrayOf(0f)
            VGL11.glGetFloatv(VGLExt.GL_MAX_TEXTURE_MAX_ANISOTROPY, anisoMax)
            VGL11.glTexParameteri(
                VGL11.GL_TEXTURE_2D, VGLExt.GL_TEXTURE_MAX_ANISOTROPY, Math.min(aniso, anisoMax[0].toInt())
            )
        }


        // VGL11.glTexParameteri(VGL11.GL_TEXTURE_2D, VGL14.GL_TEXTURE_COMPARE_MODE, VGL11.GL_NONE);
        VGL11.glBindTexture(VGL11.GL_TEXTURE_2D, 0)
    }


    fun close() {
        if (glHandle != 0) {
            VGL11.glDeleteTextures(glHandle)
        }
    }

    override fun bind(unit: Int) {
        VGL13.glActiveTexture(VGL13.GL_TEXTURE0 + unit)
        VGL11.glBindTexture(VGL11.GL_TEXTURE_2D, glHandle)
    }

}