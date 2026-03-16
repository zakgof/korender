package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.KorenderException
import com.zakgof.korender.PixelFormat
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.impl.gl.GL.glActiveTexture
import com.zakgof.korender.impl.gl.GL.glBindTexture
import com.zakgof.korender.impl.gl.GL.glDeleteTextures
import com.zakgof.korender.impl.gl.GL.glGenTextures
import com.zakgof.korender.impl.gl.GL.glGenerateMipmap
import com.zakgof.korender.impl.gl.GL.glGetError
import com.zakgof.korender.impl.gl.GL.glGetFloatv
import com.zakgof.korender.impl.gl.GL.glGetMaxTextureMaxAnisotropyConstant
import com.zakgof.korender.impl.gl.GL.glGetTextureMaxAnisotropyConstant
import com.zakgof.korender.impl.gl.GL.glPixelStorei
import com.zakgof.korender.impl.gl.GL.glTexImage3D
import com.zakgof.korender.impl.gl.GL.glTexParameteri
import com.zakgof.korender.impl.gl.GL.glTexSubImage3D
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE0
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_2D_ARRAY
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_MAG_FILTER
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_MIN_FILTER
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_WRAP_S
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_WRAP_T
import com.zakgof.korender.impl.gl.GLConstants.GL_UNPACK_ALIGNMENT
import com.zakgof.korender.impl.ignoringGlError
import com.zakgof.korender.impl.image.InternalImage
import kotlin.math.min

internal class GlGpuTextureArray(
    private val width: Int,
    private val height: Int,
    private val depth: Int,
    filter: TextureFilter = TextureFilter.MipMap,
    wrap: TextureWrap = TextureWrap.Repeat,
    aniso: Int = 1024
) : GLBindableTexture, AutoCloseable {

    override val glHandle = glGenTextures()
    private val mipmapped = filter == TextureFilter.MipMap

    private var format: PixelFormat? = null

    constructor(
        images: List<InternalImage>,
        filter: TextureFilter = TextureFilter.MipMap,
        wrap: TextureWrap = TextureWrap.Repeat,
        aniso: Int = 1024
    ) : this(
        width = images.firstOrNull()?.width ?: 0,
        height = images.firstOrNull()?.height ?: 0,
        depth = images.size,
        filter = filter,
        wrap = wrap,
        aniso = aniso
    ) {
        uploadData(images)
    }

    init {
        if (width <= 0 || height <= 0 || depth <= 0) {
            throw KorenderException("Texture array dimensions must be positive: ${width}x${height}x${depth}")
        }

        println("Creating GPU Texture Array [$this]")
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D_ARRAY, glHandle)

        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, filterMinMap[filter]!!)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, filterMagMap[filter]!!)

        val glWrap = wrapMap[wrap]!!
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, glWrap)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, glWrap)

        if (aniso > 0) {
            ignoringGlError {
                val anisoMax = glGetFloatv(glGetMaxTextureMaxAnisotropyConstant()) ?: 0f
                if (anisoMax > 0) {
                    glTexParameteri(GL_TEXTURE_2D_ARRAY, glGetTextureMaxAnisotropyConstant(), min(aniso, anisoMax.toInt()))
                }
            }
        }

        glBindTexture(GL_TEXTURE_2D_ARRAY, null)
    }

    fun uploadData(images: List<InternalImage>) {
        validateImages(images)
        val glFormat = formatMap[images.first().format]!!

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D_ARRAY, glHandle)
        if (upload(images, glFormat)) {
            if (mipmapped) {
                glGenerateMipmap(GL_TEXTURE_2D_ARRAY)
            }
            glBindTexture(GL_TEXTURE_2D_ARRAY, null)
            return
        }
        throw KorenderException("Could not upload texture array data")
    }

    private fun validateImages(images: List<InternalImage>) {
        if (images.isEmpty()) {
            throw KorenderException("Texture array requires at least one image")
        }
        if (images.size != depth) {
            throw KorenderException("Texture array layer count mismatch: expected $depth, got ${images.size}")
        }
        val first = images.first()
        if (first.width != width || first.height != height) {
            throw KorenderException("Texture array size mismatch: expected ${width}x${height}, got ${first.width}x${first.height}")
        }
        if (images.any { it.width != width || it.height != height }) {
            throw KorenderException("All texture array layers must have identical dimensions")
        }
        if (images.any { it.format != first.format }) {
            throw KorenderException("All texture array layers must have identical pixel format")
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun upload(images: List<InternalImage>, glFormat: GlGpuTexture.GlFormat): Boolean {
        if (this.format == null) {
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
            glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, glFormat.internal, width, height, depth, 0, glFormat.format, glFormat.type, null)
            val error = glGetError()
            if (error != 0) {
                println("Could not allocate texture array with internal format 0x${glFormat.internal.toHexString()} - error 0x${error.toHexString()}")
                return false
            }
        }

        images.forEachIndexed { layer, image ->
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
            glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, layer, width, height, 1, glFormat.format, glFormat.type, image.bytes.rewind())
            val error = glGetError()
            if (error != 0) {
                println("Could not upload texture array layer $layer - error 0x${error.toHexString()}")
                return false
            }
        }

        this.format = backFormatMap[glFormat.format]
        return true
    }

    override fun bind(unit: Int) {
        glActiveTexture(GL_TEXTURE0 + unit)
        glBindTexture(GL_TEXTURE_2D_ARRAY, glHandle)
    }

    override fun close() {
        println("Destroying GPU Texture Array $this")
        glDeleteTextures(glHandle)
    }

    override fun toString() = "$glHandle"
}
