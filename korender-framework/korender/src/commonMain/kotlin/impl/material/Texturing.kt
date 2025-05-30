package com.zakgof.korender.impl.material

import com.zakgof.korender.CubeTextureDeclaration
import com.zakgof.korender.CubeTextureImages
import com.zakgof.korender.CubeTextureResources
import com.zakgof.korender.CubeTextureSide
import com.zakgof.korender.KorenderException
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

    suspend fun create(declaration: InternalTexture, appResourceLoader: ResourceLoader): GlGpuTexture {

        val image = when (declaration) {
            is ByteArrayTextureDeclaration -> Platform.loadImage(declaration.fileBytes, declaration.extension).await()
            is ResourceTextureDeclaration -> Platform.loadImage(
                resourceBytes(appResourceLoader, declaration.textureResource),
                declaration.textureResource.split(".").last()
            ).await()
            is ImageTextureDeclaration -> declaration.image
            else -> throw KorenderException("Internal error")
        }
        return GlGpuTexture(
            image,
            declaration.filter,
            declaration.wrap,
            declaration.aniso
        )
    }


    suspend fun cube(decl: ResourceCubeTextureDeclaration, appResourceLoader: ResourceLoader): GlGpuCubeTexture {
        val images = CubeTextureSide.entries
            .map { toImage(appResourceLoader, decl.resources[it]!!) }
            .awaitAll()
            .let { CubeTextureSide.entries.zip(it).toMap() }
        return GlGpuCubeTexture(images)
    }

    private suspend fun toImage(appResourceLoader: ResourceLoader, resource: String): Deferred<InternalImage> {
        val bytes = resourceBytes(appResourceLoader, resource)
        val extension = resource.split(".").last()
        return Platform.loadImage(bytes, extension)
    }

    fun cube(decl: ImageCubeTextureDeclaration): GlGpuCubeTexture =
        GlGpuCubeTexture(decl.images)

}

internal interface InternalTexture : Retentionable {
    val filter: TextureFilter
    val wrap: TextureWrap
    val aniso: Int
}

internal class ResourceTextureDeclaration(
    val textureResource: String,
    override val filter: TextureFilter = TextureFilter.MipMap,
    override val wrap: TextureWrap = TextureWrap.Repeat,
    override val aniso: Int = 1024,
    override val retentionPolicy: RetentionPolicy
) : TextureDeclaration, InternalTexture {
    override fun equals(other: Any?): Boolean =
        (other is ResourceTextureDeclaration && other.textureResource == textureResource)

    override fun hashCode(): Int = textureResource.hashCode()
}

internal class ImageTextureDeclaration(
    val id: String,
    val image: InternalImage,
    override val filter: TextureFilter = TextureFilter.MipMap,
    override val wrap: TextureWrap = TextureWrap.Repeat,
    override val aniso: Int = 1024,
    override val retentionPolicy: RetentionPolicy
) : TextureDeclaration, InternalTexture {
    override fun equals(other: Any?): Boolean =
        (other is ImageTextureDeclaration && other.id == id)
    override fun hashCode(): Int = id.hashCode()
}

internal data class ProbeTextureDeclaration (val frameProbeName: String) : TextureDeclaration

internal class ByteArrayTextureDeclaration(
    private val id: String,
    override val filter: TextureFilter,
    override val wrap: TextureWrap,
    override val aniso: Int,
    val fileBytes: ByteArray,
    val extension: String,
    override val retentionPolicy: RetentionPolicy
) : TextureDeclaration, InternalTexture {
    override fun equals(other: Any?): Boolean =
        (other is ByteArrayTextureDeclaration && other.id == id)

    override fun hashCode(): Int = id.hashCode()
}

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
