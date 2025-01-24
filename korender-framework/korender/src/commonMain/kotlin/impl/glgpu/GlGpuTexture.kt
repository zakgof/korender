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
import com.zakgof.korender.impl.gl.GL.glTexParameterfv
import com.zakgof.korender.impl.gl.GL.glTexParameteri
import com.zakgof.korender.impl.gl.GLConstants.GL_CLAMP_TO_BORDER
import com.zakgof.korender.impl.gl.GLConstants.GL_CLAMP_TO_EDGE
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_COMPONENT
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_COMPONENT16
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_COMPONENT24
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_COMPONENT32
import com.zakgof.korender.impl.gl.GLConstants.GL_FLOAT
import com.zakgof.korender.impl.gl.GLConstants.GL_LINEAR
import com.zakgof.korender.impl.gl.GLConstants.GL_LINEAR_MIPMAP_LINEAR
import com.zakgof.korender.impl.gl.GLConstants.GL_MAX_TEXTURE_MAX_ANISOTROPY
import com.zakgof.korender.impl.gl.GLConstants.GL_MIRRORED_REPEAT
import com.zakgof.korender.impl.gl.GLConstants.GL_NEAREST
import com.zakgof.korender.impl.gl.GLConstants.GL_REPEAT
import com.zakgof.korender.impl.gl.GLConstants.GL_RG
import com.zakgof.korender.impl.gl.GLConstants.GL_RG16
import com.zakgof.korender.impl.gl.GLConstants.GL_RG8
import com.zakgof.korender.impl.gl.GLConstants.GL_RGB
import com.zakgof.korender.impl.gl.GLConstants.GL_RGB16
import com.zakgof.korender.impl.gl.GLConstants.GL_RGBA
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE0
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_2D
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_BORDER_COLOR
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_MAG_FILTER
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_MAX_ANISOTROPY
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_MIN_FILTER
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_WRAP_S
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_WRAP_T
import com.zakgof.korender.impl.gl.GLConstants.GL_UNSIGNED_BYTE
import com.zakgof.korender.impl.gl.GLConstants.GL_UNSIGNED_INT
import com.zakgof.korender.impl.gl.GLConstants.GL_UNSIGNED_SHORT
import com.zakgof.korender.impl.gl.GLTexture
import com.zakgof.korender.impl.ignoringGlError
import com.zakgof.korender.impl.image.InternalImage
import kotlin.math.min

private val filterMinMap = mapOf(
    TextureFilter.Nearest to GL_NEAREST,
    TextureFilter.Linear to GL_LINEAR,
    TextureFilter.MipMap to GL_LINEAR_MIPMAP_LINEAR,
)

private val filterMagMap = mapOf(
    TextureFilter.Nearest to GL_NEAREST,
    TextureFilter.Linear to GL_LINEAR,
    TextureFilter.MipMap to GL_LINEAR,
)

private val wrapMap = mapOf(
    TextureWrap.MirroredRepeat to GL_MIRRORED_REPEAT,
    TextureWrap.ClampToEdge to GL_CLAMP_TO_EDGE,
    TextureWrap.ClampToBorder to GL_CLAMP_TO_BORDER,
    TextureWrap.Repeat to GL_REPEAT
)

private val formatMap = mapOf(
    Image.Format.RGBA to GL_RGBA,
    Image.Format.RGB to GL_RGB,
)

internal class GlGpuTexture(
    private val name: String,
    image: InternalImage?,
    width: Int,
    height: Int,
    filter: TextureFilter,
    wrap: TextureWrap,
    aniso: Int,
    formats: List<GlFormat>,
) : AutoCloseable {

    val glHandle: GLTexture = glGenTextures()
    val mipmapped = filter == TextureFilter.MipMap

    init {
        println("Creating GPU Texture $this")

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, glHandle)

        initTexImage(formats, width, height, image)

        if (filter == TextureFilter.MipMap && image != null) {
            glGenerateMipmap(GL_TEXTURE_2D)
        }

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filterMinMap[filter]!!)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filterMagMap[filter]!!)

        val glWrap = wrapMap[wrap]!!
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, glWrap)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, glWrap)

        if (glWrap == GL_CLAMP_TO_BORDER) {
            glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, floatArrayOf(1f, 1f, 1f, 1f))
        }

        if (aniso > 0) {
            ignoringGlError {
                val anisoMax = glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY) ?: 0f
                if (anisoMax > 0) {
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY, min(aniso, anisoMax.toInt()))
                }
            }
        }
    }

    constructor(
        name: String,
        image: InternalImage,
        filter: TextureFilter = TextureFilter.MipMap,
        wrap: TextureWrap = TextureWrap.Repeat,
        aniso: Int = 1024
    ) : this(
        name, image, image.width, image.height, filter, wrap, aniso, listOf(
            GlFormat(formatMap[image.format]!!, formatMap[image.format]!!, GL_UNSIGNED_BYTE)
        )
    )

    constructor(
        name: String,
        width: Int,
        height: Int,
        preset: Preset
    ) : this(
        name, null, width, height, preset.filter, preset.wrap, preset.aniso, preset.formats
    )

    @OptIn(ExperimentalStdlibApi::class)
    private fun initTexImage(formats: List<GlFormat>, width: Int, height: Int, image: InternalImage?) {
        for (glFormat in formats) {
            glTexImage2D(GL_TEXTURE_2D, 0, glFormat.internal, width, height, 0, glFormat.format, glFormat.type, image?.bytes)
            val errcode = glGetError()
            if (errcode != 0) {
                println("Could not create a texture with format 0x${glFormat.internal.toHexString()}. Falling back to next format when creating texture")
                continue
            }
            return
        }
        throw KorenderException("Could not create GL texture")
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

    enum class Preset(val filter: TextureFilter, val wrap: TextureWrap, val aniso: Int, val formats: List<GlFormat>) {
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
//                GlFormat(GL_RG16F, GL_RG, GL_FLOAT),
                GlFormat(GL_RGB16, GL_RGB, GL_UNSIGNED_SHORT), // TODO ESM

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
}