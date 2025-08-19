package com.zakgof.korender.impl.material

import com.zakgof.korender.CubeTextureDeclaration
import com.zakgof.korender.CubeTextureImages
import com.zakgof.korender.CubeTextureResources
import com.zakgof.korender.CubeTextureSide
import com.zakgof.korender.Platform
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.Texture3DDeclaration
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.impl.buffer.NativeFloatBuffer
import com.zakgof.korender.impl.engine.Loader
import com.zakgof.korender.impl.engine.Retentionable
import com.zakgof.korender.impl.gl.GLConstants.GL_FLOAT
import com.zakgof.korender.impl.gl.GLConstants.GL_RGBA
import com.zakgof.korender.impl.gl.GLConstants.GL_RGBA32F
import com.zakgof.korender.impl.glgpu.GlGpuCubeTexture
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.glgpu.GlGpuTexture3D
import com.zakgof.korender.impl.image.InternalImage
import com.zakgof.korender.impl.image.impl.image.InternalImage3D

object NotYetLoadedTexture

internal object Texturing {

    fun cube(decl: ResourceCubeTextureDeclaration, loader: Loader): GlGpuCubeTexture? {
        val resources = CubeTextureSide.entries.map { decl.resources[it]!! }
        val byteArrays = resources.map { it to loader.unsafeBytes(it) }
        val images = byteArrays
            .filter { it.second != null }
            .mapNotNull { loader.unsafeWait(it.first) { Platform.loadImage(it.second!!, it.first.split(".").last()) } }
        return if (images.size == 6) {
            resources.forEach {
                loader.free(it)
            }
            GlGpuCubeTexture(CubeTextureSide.entries.zip(images).toMap())
        } else
            null
    }

    fun toImage(loader: Loader, resource: String): InternalImage? =
        loader.safeBytes(resource) {
            loader.wait(resource) { Platform.loadImage(it, resource.split(".").last()) }
        }

    fun cube(decl: ImageCubeTextureDeclaration): GlGpuCubeTexture =
        GlGpuCubeTexture(decl.images)

}

internal interface InternalTexture : Retentionable {
    fun generateGpuTexture(loader: Loader): GlGpuTexture?
}

internal class ResourceTextureDeclaration(
    val textureResource: String,
    val filter: TextureFilter = TextureFilter.MipMap,
    private val wrap: TextureWrap = TextureWrap.Repeat,
    private val aniso: Int = 1024,
    override val retentionPolicy: RetentionPolicy
) : TextureDeclaration, InternalTexture {
    override fun equals(other: Any?): Boolean =
        (other is ResourceTextureDeclaration && other.textureResource == textureResource)

    override fun hashCode(): Int = textureResource.hashCode()

    override fun generateGpuTexture(loader: Loader): GlGpuTexture? =
        Texturing.toImage(loader, textureResource)?.let {
            GlGpuTexture(it, filter, wrap, aniso)
        }
}

internal class ImageTextureDeclaration(
    val id: String,
    val image: InternalImage,
    val filter: TextureFilter,
    private val wrap: TextureWrap,
    private val aniso: Int,
    override val retentionPolicy: RetentionPolicy
) : TextureDeclaration, InternalTexture {
    override fun equals(other: Any?): Boolean =
        (other is ImageTextureDeclaration && other.id == id)

    override fun hashCode(): Int = id.hashCode()

    override fun generateGpuTexture(loader: Loader): GlGpuTexture =
        GlGpuTexture(image, filter, wrap, aniso)
}

internal class ImageTexture3DDeclaration(
    val id: String,
    val image: InternalImage3D,
    val filter: TextureFilter,
    private val wrap: TextureWrap,
    private val aniso: Int,
    override val retentionPolicy: RetentionPolicy
) : Texture3DDeclaration, Retentionable {
    override fun equals(other: Any?): Boolean =
        (other is ImageTexture3DDeclaration && other.id == id)

    override fun hashCode(): Int = id.hashCode()

    fun generateGpuTexture3D(loader: Loader): GlGpuTexture3D =
        GlGpuTexture3D(image, filter, wrap, aniso)
}

internal class ByteArrayTextureDeclaration(
    private val id: String,
    val filter: TextureFilter,
    val wrap: TextureWrap,
    val aniso: Int,
    val fileBytesLoader: () -> ByteArray,
    val extension: String,
    override val retentionPolicy: RetentionPolicy
) : TextureDeclaration, InternalTexture {

    override fun equals(other: Any?): Boolean =
        (other is ByteArrayTextureDeclaration && other.id == id)

    override fun hashCode(): Int = id.hashCode()

    override fun generateGpuTexture(loader: Loader): GlGpuTexture? =
        loader.wait("@$id") {
            Platform.loadImage(fileBytesLoader(), extension)
        }?.let {
            GlGpuTexture(it, filter, wrap, aniso)
        }
}

internal class TextureLinkDeclaration(
    private val id: String,
    val width: Int,
    val height: Int,
    override val retentionPolicy: RetentionPolicy
) : TextureDeclaration, InternalTexture {
    override fun equals(other: Any?): Boolean =
        (other is TextureLinkDeclaration && other.id == id)

    override fun hashCode(): Int = id.hashCode()

    override fun generateGpuTexture(loader: Loader): GlGpuTexture =
        GlGpuTexture(width, height, TextureFilter.Nearest, TextureWrap.Repeat, 0)

    fun generateGpuTextureLink(loader: Loader) =
        GpuTextureLink(generateGpuTexture(loader), NativeFloatBuffer(width * height * 4))
}

internal data class ProbeTextureDeclaration(val frameProbeName: String) : TextureDeclaration


internal data class ResourceCubeTextureDeclaration(val resources: CubeTextureResources, override val retentionPolicy: RetentionPolicy) : CubeTextureDeclaration, Retentionable {
    override fun equals(other: Any?): Boolean =
        (other is ResourceCubeTextureDeclaration && other.resources == resources)

    override fun hashCode(): Int = resources.hashCode()
}

internal class ImageCubeTextureDeclaration(
    val id: String,
    val images: CubeTextureImages,
    override val retentionPolicy: RetentionPolicy
) : CubeTextureDeclaration, Retentionable {
    override fun equals(other: Any?): Boolean =
        (other is ImageCubeTextureDeclaration && other.id == id)

    override fun hashCode(): Int = id.hashCode()
}

internal data class ProbeCubeTextureDeclaration(val envProbeName: String) : CubeTextureDeclaration

internal class GpuTextureLink(
    val texture: GlGpuTexture,
    val buffer: NativeFloatBuffer
) : AutoCloseable {
    override fun close() =
        texture.close()
    fun uploadData() =
        texture.uploadData(buffer.rewind(), GlGpuTexture.GlFormat(GL_RGBA32F, GL_RGBA, GL_FLOAT))
}
