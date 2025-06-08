package com.zakgof.korender.impl.material

import com.zakgof.korender.CubeTextureDeclaration
import com.zakgof.korender.CubeTextureImages
import com.zakgof.korender.CubeTextureResources
import com.zakgof.korender.CubeTextureSide
import com.zakgof.korender.Platform
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.impl.engine.Loader
import com.zakgof.korender.impl.glgpu.GlGpuCubeTexture
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.image.InternalImage
import impl.engine.Retentionable

object NotYetLoadedTexture
object NotYetLoadedCubeTexture

internal object Texturing {

    fun cube(decl: ResourceCubeTextureDeclaration, loader: Loader): GlGpuCubeTexture? {
        val images = CubeTextureSide.entries.mapNotNull { toImage(loader, decl.resources[it]!!) }
        return if (images.size == 6)
            GlGpuCubeTexture(CubeTextureSide.entries.zip(images).toMap())
        else
            null
    }

    fun toImage(loader: Loader, resource: String): InternalImage? =
        loader.load(resource)?.let {
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

internal class RawTextureDeclaration(
    private val id: String,
    val width: Int,
    val height: Int,
    override val retentionPolicy: RetentionPolicy
) : TextureDeclaration, InternalTexture {
    override fun equals(other: Any?): Boolean =
        (other is RawTextureDeclaration && other.id == id)

    override fun hashCode(): Int = id.hashCode()

    override fun generateGpuTexture(loader: Loader): GlGpuTexture =
        GlGpuTexture(width, height, TextureFilter.Nearest, TextureWrap.Repeat, 0)
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
