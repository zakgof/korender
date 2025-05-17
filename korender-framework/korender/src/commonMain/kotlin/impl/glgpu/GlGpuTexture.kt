package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.Image
import com.zakgof.korender.KorenderException
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.impl.buffer.NativeByteBuffer
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

        if (mipmapped && image != null) {
            glGenerateMipmap(GL_TEXTURE_2D)
        }

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filterMinMap[filter]!!)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filterMagMap[filter]!!)

        val glWrap = wrapMap[wrap]!!
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, glWrap)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, glWrap)

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
        name, image, image.width, image.height, filter, wrap, aniso, listOf(formatMap[image.format]!!)
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
        SimpleDepth(
            TextureFilter.Nearest, TextureWrap.Repeat, 0,
            listOf(
                GlFormat(GL_DEPTH_COMPONENT16, GL_DEPTH_COMPONENT, GL_UNSIGNED_SHORT)
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
        fun zeroTex(): GlGpuTexture = GlGpuTexture(
            "zero", InternalImage(
                2, 2, NativeByteBuffer(14).apply {
                    put(
                        byteArrayOf(
                            0, 0, 0, 255.toByte(), 0, 0,
                            0, 255.toByte(), 0, 0, 0, 0, 0, 0
                        )
                    )
                    rewind()
                }, Image.Format.RGB
            )
        )
    }
}