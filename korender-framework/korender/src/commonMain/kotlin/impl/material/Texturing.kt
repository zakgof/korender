package com.zakgof.korender.impl.material

import com.zakgof.korender.material.TextureDeclaration
import com.zakgof.korender.material.TextureFilter
import com.zakgof.korender.material.TextureWrap
import com.zakgof.korender.getPlatform
import com.zakgof.korender.image.Image
import com.zakgof.korender.image.Images
import com.zakgof.korender.impl.ResourceLoader
import com.zakgof.korender.impl.gpu.Gpu
import com.zakgof.korender.impl.gpu.GpuTexture
import com.zakgof.korender.impl.resourceBytes

object NotYetLoadedTexture

internal object Texturing {

    suspend fun create(declaration: TextureDeclaration, gpu: Gpu, appResourceLoader: ResourceLoader): GpuTexture {
        val image = Images.image(appResourceLoader, declaration.textureResource).await()
        return create(declaration.textureResource, image, gpu, declaration.filter, declaration.wrap, declaration.aniso)
    }

    fun create(name: String, image: Image, gpu: Gpu, filter: TextureFilter = TextureFilter.MipMapLinearLinear, wrap: TextureWrap = TextureWrap.Repeat, aniso: Int = 1024): GpuTexture {
        return gpu.createTexture(
            name, image.width, image.height, image.bytes, filter, wrap, aniso, image.format
        )
    }
}
