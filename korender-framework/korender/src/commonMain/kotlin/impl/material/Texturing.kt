package com.zakgof.korender.impl.material

import com.zakgof.korender.CubeTextureDeclaration
import com.zakgof.korender.KorenderException
import com.zakgof.korender.Platform
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.impl.glgpu.GlGpuCubeTexture
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.image.InternalImage
import com.zakgof.korender.impl.resourceBytes
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll

object NotYetLoadedTexture

internal object Texturing {

    suspend fun create(declaration: InternalTexture, appResourceLoader: ResourceLoader): GlGpuTexture {

        val bytes = when (declaration) {
            is ByteArrayTextureDeclaration -> declaration.fileBytes
            is ResourceTextureDeclaration -> resourceBytes(
                appResourceLoader,
                declaration.textureResource
            )

            else -> throw KorenderException("Internal error")
        }

        val extension = when (declaration) {
            is ByteArrayTextureDeclaration -> declaration.extension
            is ResourceTextureDeclaration -> declaration.textureResource.split(".").last()
            else -> throw KorenderException("Internal error")
        }

        val image = Platform.loadImage(bytes, extension).await()

        return GlGpuTexture(
            declaration.id, image,
            declaration.filter,
            declaration.wrap,
            declaration.aniso
        )
    }

    suspend fun cube(decl: ResourceCubeTextureDeclaration, appResourceLoader: ResourceLoader): GlGpuCubeTexture {
        val images = listOf(
            toImage(appResourceLoader, decl.nxResource),
            toImage(appResourceLoader, decl.nyResource),
            toImage(appResourceLoader, decl.nzResource),
            toImage(appResourceLoader, decl.pxResource),
            toImage(appResourceLoader, decl.pyResource),
            toImage(appResourceLoader, decl.pzResource)
        ).awaitAll()
        return GlGpuCubeTexture(images[0], images[1], images[2], images[3], images[4], images[5])
    }

    private suspend fun toImage(appResourceLoader: ResourceLoader, resource: String): Deferred<InternalImage> {
        val bytes = resourceBytes(appResourceLoader, resource)
        val extension = resource.split(".").last()
        return Platform.loadImage(bytes, extension)
    }
}

internal interface InternalTexture {

    val id: String
    val filter: TextureFilter
    val wrap: TextureWrap
    val aniso: Int
}

internal class ResourceTextureDeclaration(
    val textureResource: String,
    override val filter: TextureFilter = TextureFilter.MipMap,
    override val wrap: TextureWrap = TextureWrap.Repeat,
    override val aniso: Int = 1024
) : TextureDeclaration, InternalTexture {
    override val id = textureResource
    override fun equals(other: Any?): Boolean =
        (other is ResourceTextureDeclaration && other.textureResource == textureResource)

    override fun hashCode(): Int = textureResource.hashCode()
}

internal class ByteArrayTextureDeclaration(
    override val id: String,
    override val filter: TextureFilter,
    override val wrap: TextureWrap,
    override val aniso: Int,
    val fileBytes: ByteArray,
    val extension: String
) : TextureDeclaration, InternalTexture {
    override fun equals(other: Any?): Boolean =
        (other is ByteArrayTextureDeclaration && other.id == id)

    override fun hashCode(): Int = id.hashCode()
}

internal data class ResourceCubeTextureDeclaration(
    val nxResource: String,
    val nyResource: String,
    val nzResource: String,
    val pxResource: String,
    val pyResource: String,
    val pzResource: String
) : CubeTextureDeclaration
