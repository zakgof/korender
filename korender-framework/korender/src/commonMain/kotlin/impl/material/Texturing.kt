package com.zakgof.korender.impl.material

import com.zakgof.korender.KorenderException
import com.zakgof.korender.Platform
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.resourceBytes
import com.zakgof.korender.material.TextureDeclaration
import com.zakgof.korender.material.TextureFilter
import com.zakgof.korender.material.TextureWrap

object NotYetLoadedTexture

internal object Texturing {

    suspend fun create(
        declaration: InternalTexture,
        appResourceLoader: ResourceLoader
    ): GlGpuTexture {

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
}

internal interface InternalTexture {
    val id: String
    val filter: TextureFilter
    val wrap: TextureWrap
    val aniso: Int
}

internal class ResourceTextureDeclaration(
    val textureResource: String,
    override val filter: TextureFilter = TextureFilter.MipMapLinearLinear,
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
