package com.zakgof.korender.impl.material

import com.zakgof.korender.CubeTextureDeclaration
import com.zakgof.korender.CubeTextureImages
import com.zakgof.korender.CubeTextureResources
import com.zakgof.korender.CubeTextureSide
import com.zakgof.korender.Platform
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.impl.glgpu.GlGpuCubeTexture
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.image.InternalImage
import com.zakgof.korender.impl.resourceBytes
import impl.engine.Retentionable
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll

object NotYetLoadedTexture
object NotYetLoadedCubeTexture

internal object Texturing {

    suspend fun cube(decl: ResourceCubeTextureDeclaration, appResourceLoader: ResourceLoader): GlGpuCubeTexture {
        val images = CubeTextureSide.entries
            .map { toImage(appResourceLoader, decl.resources[it]!!) }
            .awaitAll()
            .let { CubeTextureSide.entries.zip(it).toMap() }
        return GlGpuCubeTexture(images)
    }

    suspend fun toImage(appResourceLoader: ResourceLoader, resource: String): Deferred<InternalImage> {
        val bytes = resourceBytes(appResourceLoader, resource)
        val extension = resource.split(".").last()
        return Platform.loadImage(bytes, extension)
    }

    fun cube(decl: ImageCubeTextureDeclaration): GlGpuCubeTexture =
        GlGpuCubeTexture(decl.images)

}

internal interface InternalTexture : Retentionable {
    suspend fun generateGpuTexture(appResourceLoader: ResourceLoader): GlGpuTexture
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

    override suspend fun generateGpuTexture(appResourceLoader: ResourceLoader): GlGpuTexture {
        val image = Texturing.toImage(appResourceLoader, textureResource).await()
        return GlGpuTexture(image, filter, wrap, aniso)
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

    override suspend fun generateGpuTexture(appResourceLoader: ResourceLoader) =
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

    override suspend fun generateGpuTexture(appResourceLoader: ResourceLoader): GlGpuTexture {
        val image = Platform.loadImage(fileBytesLoader(), extension).await()
        return GlGpuTexture(image, filter, wrap, aniso)
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

    override suspend fun generateGpuTexture(appResourceLoader: ResourceLoader) =
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
