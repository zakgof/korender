package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.KorenderException
import com.zakgof.korender.PixelFormat
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.impl.buffer.NativeBuffer
import com.zakgof.korender.impl.gl.GL.glActiveTexture
import com.zakgof.korender.impl.gl.GL.glBindTexture
import com.zakgof.korender.impl.gl.GL.glDeleteTextures
import com.zakgof.korender.impl.gl.GL.glGenTextures
import com.zakgof.korender.impl.gl.GL.glGenerateMipmap
import com.zakgof.korender.impl.gl.GL.glGetError
import com.zakgof.korender.impl.gl.GL.glGetFloatv
import com.zakgof.korender.impl.gl.GL.glGetMaxTextureMaxAnisotropyConstant
import com.zakgof.korender.impl.gl.GL.glGetTextureMaxAnisotropyConstant
import com.zakgof.korender.impl.gl.GL.glTexImage3D
import com.zakgof.korender.impl.gl.GL.glTexParameteri
import com.zakgof.korender.impl.gl.GL.glTexSubImage3D
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE0
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_3D
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_MAG_FILTER
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_MIN_FILTER
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_WRAP_S
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_WRAP_T
import com.zakgof.korender.impl.ignoringGlError
import com.zakgof.korender.impl.image.impl.image.InternalImage3D
import kotlin.math.min

internal class GlGpuTexture3D(private val width: Int, private val height: Int, private val depth: Int, filter: TextureFilter = TextureFilter.MipMap, wrap: TextureWrap = TextureWrap.Repeat, aniso: Int = 1024) : GLBindableTexture, AutoCloseable {

    override val glHandle = glGenTextures()
    val mipmapped = filter == TextureFilter.MipMap

    private var format: PixelFormat? = null
    private lateinit var glFormat: GlGpuTexture.GlFormat

    constructor(image: InternalImage3D, filter: TextureFilter = TextureFilter.MipMap, wrap: TextureWrap = TextureWrap.Repeat, aniso: Int = 1024) : this(image.width, image.height, image.depth, filter, wrap, aniso) {
        uploadData(image.bytes, formatMap[image.format]!!)
    }

    init {
        println("Creating GPU 3D Texture [$this]")
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_3D, glHandle)

        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, filterMinMap[filter]!!)
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, filterMagMap[filter]!!)

        val glWrap = wrapMap[wrap]!!
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, glWrap)
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, glWrap)

        if (aniso > 0) {
            ignoringGlError {
                val anisoMax = glGetFloatv(glGetMaxTextureMaxAnisotropyConstant()) ?: 0f
                if (anisoMax > 0) {
                    glTexParameteri(GL_TEXTURE_3D, glGetTextureMaxAnisotropyConstant(), min(aniso, anisoMax.toInt()))
                }
            }
        }

        glBindTexture(GL_TEXTURE_3D, null)
    }

    fun uploadData(buffer: NativeBuffer?, format: GlGpuTexture.GlFormat) {
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_3D, glHandle)
        if (upload(width, height, buffer, format)) {
            if (mipmapped) {
                glGenerateMipmap(GL_TEXTURE_3D)
            }
            glBindTexture(GL_TEXTURE_3D, null)
            return
        }
        throw KorenderException("Could not upload texture data")
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun upload(width: Int, height: Int, buffer: NativeBuffer?, format: GlGpuTexture.GlFormat): Boolean {

        if (this.format != null && buffer != null) {
            glTexSubImage3D(GL_TEXTURE_3D, 0, 0, 0, 0, width, height, depth, format.format, format.type, buffer.rewind())
            return true
        }

        glTexImage3D(GL_TEXTURE_3D, 0, format.internal, width, height, depth, 0, format.format, format.type, buffer?.rewind())
        val error = glGetError()
        if (error != 0) {
            println("Could not upload 3D texture data with internal format 0x${format.internal.toHexString()} - error 0x${error.toHexString()}")
            return false
        }
        this.glFormat = format
        this.format = backFormatMap[format.format]
        return true
    }

    override fun bind(unit: Int) {
        glActiveTexture(GL_TEXTURE0 + unit)
        glBindTexture(GL_TEXTURE_3D, glHandle)
    }

    override fun close() {
        println("Destroying GPU 3D Texture $this")
        glDeleteTextures(glHandle)
    }

    override fun toString() = "$glHandle"
}