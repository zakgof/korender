package com.zakgof.korender.impl.material

import com.zakgof.korender.declaration.TextureDeclaration
import com.zakgof.korender.declaration.TextureFilter
import com.zakgof.korender.declaration.TextureWrap
import com.zakgof.korender.getPlatform
import com.zakgof.korender.impl.gpu.Gpu
import com.zakgof.korender.impl.gpu.GpuTexture
import com.zakgof.korender.impl.resourceStream

internal object Texturing {
    fun create(declaration: TextureDeclaration, gpu: Gpu): GpuTexture {
        val image = getPlatform().loadImage(resourceStream(declaration.textureResource))
        return create(image, gpu, declaration.filter, declaration.wrap, declaration.aniso)
    }

    fun create(image: Image, gpu: Gpu, filter: TextureFilter = TextureFilter.MipMapLinearLinear, wrap: TextureWrap = TextureWrap.Repeat, aniso: Int = 1024): GpuTexture {
        return gpu.createTexture(
            image.width,
            image.height,
            image.bytes,
            filter,
            wrap,
            aniso,
            image.format
        )
    }
}
