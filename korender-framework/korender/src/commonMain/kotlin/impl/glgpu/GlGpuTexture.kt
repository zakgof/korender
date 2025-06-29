package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.Image
import com.zakgof.korender.KorenderException
import com.zakgof.korender.Platform
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.impl.buffer.NativeBuffer
import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.impl.gl.GL.glActiveTexture
import com.zakgof.korender.impl.gl.GL.glBindTexture
import com.zakgof.korender.impl.gl.GL.glDeleteTextures
import com.zakgof.korender.impl.gl.GL.glGenTextures
import com.zakgof.korender.impl.gl.GL.glGenerateMipmap
import com.zakgof.korender.impl.gl.GL.glGetError
import com.zakgof.korender.impl.gl.GL.glGetFloatv
import com.zakgof.korender.impl.gl.GL.glGetMaxTextureMaxAnisotropyConstant
import com.zakgof.korender.impl.gl.GL.glGetTexImage
import com.zakgof.korender.impl.gl.GL.glGetTextureMaxAnisotropyConstant
import com.zakgof.korender.impl.gl.GL.glTexImage2D
import com.zakgof.korender.impl.gl.GL.glTexParameteri
import com.zakgof.korender.impl.gl.GL.glTexSubImage2D
import com.zakgof.korender.impl.gl.GLConstants.GL_CLAMP_TO_EDGE
import com.zakgof.korender.impl.gl.GLConstants.GL_COMPARE_REF_TO_TEXTURE
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_COMPONENT
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_COMPONENT16
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_COMPONENT24
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_COMPONENT32
import com.zakgof.korender.impl.gl.GLConstants.GL_FLOAT
import com.zakgof.korender.impl.gl.GLConstants.GL_LEQUAL
import com.zakgof.korender.impl.gl.GLConstants.GL_LINEAR
import com.zakgof.korender.impl.gl.GLConstants.GL_LINEAR_MIPMAP_LINEAR
import com.zakgof.korender.impl.gl.GLConstants.GL_MIRRORED_REPEAT
import com.zakgof.korender.impl.gl.GLConstants.GL_NEAREST
import com.zakgof.korender.impl.gl.GLConstants.GL_R16
import com.zakgof.korender.impl.gl.GLConstants.GL_R8
import com.zakgof.korender.impl.gl.GLConstants.GL_RED
import com.zakgof.korender.impl.gl.GLConstants.GL_REPEAT
import com.zakgof.korender.impl.gl.GLConstants.GL_RG
import com.zakgof.korender.impl.gl.GLConstants.GL_RG16
import com.zakgof.korender.impl.gl.GLConstants.GL_RG8
import com.zakgof.korender.impl.gl.GLConstants.GL_RGB
import com.zakgof.korender.impl.gl.GLConstants.GL_RGBA
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE0
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_2D
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_COMPARE_FUNC
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_COMPARE_MODE
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_MAG_FILTER
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_MIN_FILTER
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_WRAP_S
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_WRAP_T
import com.zakgof.korender.impl.gl.GLConstants.GL_UNSIGNED_BYTE
import com.zakgof.korender.impl.gl.GLConstants.GL_UNSIGNED_INT
import com.zakgof.korender.impl.gl.GLConstants.GL_UNSIGNED_SHORT
import com.zakgof.korender.impl.ignoringGlError
import com.zakgof.korender.impl.image.InternalImage
import kotlin.math.min

internal val filterMinMap = mapOf(
    TextureFilter.Nearest to GL_NEAREST,
    TextureFilter.Linear to GL_LINEAR,
    TextureFilter.MipMap to GL_LINEAR_MIPMAP_LINEAR,
)

internal val filterMagMap = mapOf(
    TextureFilter.Nearest to GL_NEAREST,
    TextureFilter.Linear to GL_LINEAR,
    TextureFilter.MipMap to GL_LINEAR,
)

internal val wrapMap = mapOf(
    TextureWrap.MirroredRepeat to GL_MIRRORED_REPEAT,
    TextureWrap.ClampToEdge to GL_CLAMP_TO_EDGE,
    TextureWrap.Repeat to GL_REPEAT
)

internal val formatMap = mapOf(
    Image.Format.RGBA to GlGpuTexture.GlFormat(GL_RGBA, GL_RGBA, GL_UNSIGNED_BYTE),
    Image.Format.RGB to GlGpuTexture.GlFormat(GL_RGB, GL_RGB, GL_UNSIGNED_BYTE),
    Image.Format.Gray to GlGpuTexture.GlFormat(GL_R8, GL_RED, GL_UNSIGNED_BYTE),
    Image.Format.Gray16 to GlGpuTexture.GlFormat(GL_R16, GL_RED, GL_UNSIGNED_SHORT)
)

internal val backFormatMap = mapOf(
    GL_RGBA to Image.Format.RGBA,
    GL_RGB to Image.Format.RGB,
    GL_R8 to Image.Format.Gray,
    GL_R16 to Image.Format.Gray16
)

internal class GlGpuTexture(private val width: Int, private val height: Int, filter: TextureFilter = TextureFilter.MipMap, wrap: TextureWrap = TextureWrap.Repeat, aniso: Int = 1024) : GLBindableTexture, AutoCloseable {

    override val glHandle = glGenTextures()
    val mipmapped = filter == TextureFilter.MipMap

    private var format: Image.Format? = null
    private lateinit var glFormat: GlFormat

    constructor(image: InternalImage, filter: TextureFilter = TextureFilter.MipMap, wrap: TextureWrap = TextureWrap.Repeat, aniso: Int = 1024) : this(image.width, image.height, filter, wrap, aniso) {
        uploadData(image.bytes, formatMap[image.format]!!)
    }

    constructor(width: Int, height: Int, preset: Preset) : this(width, height, preset.filter, preset.wrap, preset.aniso) {
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, glHandle)
        for (format in preset.formats) {
            if (upload(width, height, null, format)) {
                glBindTexture(GL_TEXTURE_2D, null)
                return
            }
        }
        throw KorenderException("Could not create texture with preset $preset")
    }

    init {
        println("Creating GPU Texture [$this]")
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, glHandle)

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filterMinMap[filter]!!)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filterMagMap[filter]!!)

        val glWrap = wrapMap[wrap]!!
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, glWrap)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, glWrap)

        if (aniso > 0) {
            ignoringGlError {
                val anisoMax = glGetFloatv(glGetMaxTextureMaxAnisotropyConstant()) ?: 0f
                if (anisoMax > 0) {
                    glTexParameteri(GL_TEXTURE_2D, glGetTextureMaxAnisotropyConstant(), min(aniso, anisoMax.toInt()))
                }
            }
        }

        glBindTexture(GL_TEXTURE_2D, null)
    }

    fun uploadData(buffer: NativeBuffer?, format: GlFormat) {
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, glHandle)
        if (upload(width, height, buffer, format)) {
            if (mipmapped) {
                glGenerateMipmap(GL_TEXTURE_2D)
            }
            glBindTexture(GL_TEXTURE_2D, null)
            return
        }
        throw KorenderException("Could not upload texture data")
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun upload(width: Int, height: Int, buffer: NativeBuffer?, format: GlFormat): Boolean {

        if (this.format != null && buffer != null) {
            glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, format.format, format.type, buffer.rewind())
            return true
        }

        glTexImage2D(GL_TEXTURE_2D, 0, format.internal, width, height, 0, format.format, format.type, buffer?.rewind())
        val error = glGetError()
        if (error != 0) {
            println("Could not upload texture data with internal format 0x${format.internal.toHexString()} - error 0x${error.toHexString()}")
            return false
        }
        this.glFormat = format
        this.format = backFormatMap[format.format]
        return true
    }

    override fun bind(unit: Int) {
        glActiveTexture(GL_TEXTURE0 + unit)
        glBindTexture(GL_TEXTURE_2D, glHandle)
    }

    fun fetch(): Image {
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, glHandle)
        val img = Platform.createImage(width, height, format!!)
        glGetTexImage(GL_TEXTURE_2D, 0, glFormat.format, glFormat.type, img.bytes)
        glBindTexture(GL_TEXTURE_2D, null)
        return img
    }

    fun enablePcfMode() {
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, glHandle)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glBindTexture(GL_TEXTURE_2D, null)
    }

    override fun close() {
        println("Destroying GPU Texture $this")
        glDeleteTextures(glHandle)
    }

    override fun toString() = "$glHandle"

    enum class Preset(val filter: TextureFilter, val wrap: TextureWrap, val aniso: Int, val formats: List<GlFormat>) {
        RGBMipmap(TextureFilter.MipMap, TextureWrap.Repeat, 1024, listOf(GlFormat(GL_RGB, GL_RGB, GL_UNSIGNED_BYTE))),
        RGBFilter(TextureFilter.Linear, TextureWrap.MirroredRepeat, 1024, listOf(GlFormat(GL_RGB, GL_RGB, GL_UNSIGNED_BYTE))),
        RGBAFilter(TextureFilter.Linear, TextureWrap.Repeat, 0, listOf(GlFormat(GL_RGBA, GL_RGBA, GL_UNSIGNED_BYTE))),
        RGBNoFilter(TextureFilter.Nearest, TextureWrap.Repeat, 0, listOf(GlFormat(GL_RGB, GL_RGB, GL_UNSIGNED_BYTE))),
        RGBANoFilter(TextureFilter.Nearest, TextureWrap.Repeat, 0, listOf(GlFormat(GL_RGBA, GL_RGBA, GL_UNSIGNED_BYTE))),
        Depth(
            TextureFilter.Nearest, TextureWrap.Repeat, 0,
            listOf(
                GlFormat(GL_DEPTH_COMPONENT32, GL_DEPTH_COMPONENT, GL_FLOAT),
                GlFormat(GL_DEPTH_COMPONENT24, GL_DEPTH_COMPONENT, GL_UNSIGNED_INT),
                GlFormat(GL_DEPTH_COMPONENT, GL_DEPTH_COMPONENT, GL_FLOAT),
                GlFormat(GL_DEPTH_COMPONENT16, GL_DEPTH_COMPONENT, GL_UNSIGNED_SHORT),
            )
        ),
        VSM(
            TextureFilter.Linear, TextureWrap.MirroredRepeat, 1024, listOf(
                // TODO: still want this on Desktop
//               GlFormat(GL_RG32F, GL_RG, GL_FLOAT),
//               GlFormat(GL_RG16F, GL_RG, GL_FLOAT),
                GlFormat(GL_RG16, GL_RG, GL_UNSIGNED_SHORT),
                GlFormat(GL_RG8, GL_RG, GL_UNSIGNED_BYTE),
                GlFormat(GL_RGB, GL_RGB, GL_UNSIGNED_BYTE)
            )
        )
    }

    class GlFormat(
        val internal: Int,
        val format: Int,
        val type: Int
    )

    companion object {
        fun zeroTex(): GlGpuTexture = GlGpuTexture(1, 1, TextureFilter.Linear).also {
            val buffer = NativeByteBuffer(3).rewind()
            it.uploadData(buffer, GlFormat(GL_RGB, GL_RGB, GL_UNSIGNED_BYTE))
        }

        fun zeroShadowTex() = GlGpuTexture(1, 1, TextureFilter.Linear).also {
            it.enablePcfMode()
            it.uploadData(null, GlFormat(GL_DEPTH_COMPONENT16, GL_DEPTH_COMPONENT, GL_UNSIGNED_SHORT))
        }
    }
}